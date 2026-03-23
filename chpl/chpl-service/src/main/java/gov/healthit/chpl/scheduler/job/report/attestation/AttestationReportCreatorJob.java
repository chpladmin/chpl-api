package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusConcept;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.scheduler.SchedulerSecurityContextService;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.developer.attestation.AttestationCheckinReportDAO;
import gov.healthit.chpl.scheduler.job.developer.attestation.CheckInReport;
import gov.healthit.chpl.scheduler.job.developer.attestation.CheckInReportDataCollector;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "attestationReportCreatorJobLogger")
public class AttestationReportCreatorJob extends QuartzJob {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private CheckInReportDataCollector checkInReportDataCollection;

    @Autowired
    private AttestationCheckinReportDAO attestationCheckinReportDAO;

    @Autowired
    private AttestationPeriodService attestationPeriodService;

    @Autowired
    private CertificationBodyManager certificationBodyManager;

    @Autowired
    private AttestationReportDAO attestationReportDAO;

    @Autowired
    private SchedulerSecurityContextService securityContextService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Attestation Report Creator job. *********");
        securityContextService.setAdminSecurityContext();

        // We need to manually create a transaction in this case because of how
        // AOP works. When a method is annotated with @Transactional, the transaction
        // wrapper is only added if the object's proxy is called. The object's proxy is
        // not called when the method is called from within this class. The object's
        // proxy is called when the method is public and is called from a different
        // object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionOperations transactionOperations = new TransactionTemplate(transactionManager,
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        transactionOperations.executeWithoutResult(status -> {
                try {
                    LOGGER.info("Collecting checkin report data");
                    List<Long> acbIds = certificationBodyManager.getAllActive().stream()
                            .map(CertificationBody::getId)
                            .toList();
                    List<CheckInReport> checkInReportRows = checkInReportDataCollection.collect(acbIds);
                    attestationCheckinReportDAO.deleteByReportDate(LocalDate.now());
                    attestationCheckinReportDAO.save(checkInReportRows);
                    LOGGER.info("Done Collecting checkin report data");

                    //The rest of the data can all be derived using the data from the checkin report, so
                    //use the checkin report data to create the attestation report.

                    if (attestationPeriodService.isTodayDuringSubmissionPlusApprovalPeriod()) {
                        if (!CollectionUtils.isEmpty(attestationReportDAO.getAttestationReportByDate(LocalDate.now()))) {
                            attestationReportDAO.deleteAttestationReportByDate(LocalDate.now());
                        }

                        AttestationPeriod mostRecentPastAttestationPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
                        Map<Long, AttestationReport> attestationReportsByAcbId = new HashMap<Long, AttestationReport>();
                        List<CertificationBody> activeAcbs = certificationBodyManager.getAllActive();
                        Map<Long, ChangeRequest> changeRequestsByDeveloperId = new HashMap<Long, ChangeRequest>();

                        //applicableDevelopers.forEach(developer -> {
                        checkInReportRows.forEach(checkInReportRow -> {
                            LOGGER.info("Processing Developer: {} ({})", checkInReportRow.getDeveloper().getName(), checkInReportRow.getDeveloper().getId());
                            try {

                                activeAcbs.forEach(acb -> {
                                    if (isDeveloperManagedByAcb(checkInReportRow, acb)) {
                                       if (!attestationReportsByAcbId.containsKey(acb.getId())) {
                                           attestationReportsByAcbId.put(acb.getId(),
                                                   AttestationReport.builder()
                                                           .attestationPeriod(mostRecentPastAttestationPeriod)
                                                           .certificationBody(acb)
                                                           .reportDate(LocalDate.now())
                                                           .build());
                                       }
                                       AttestationReport report = attestationReportsByAcbId.get(acb.getId());
                                       report.setDeveloperCount(report.getDeveloperCount() + 1);
                                       updateCountsBasedOnChangeRequestStatus(checkInReportRow, report);
                                   }
                                });

                            } catch (Exception e) {
                                LOGGER.error("Could not collect Developer Attestation Report data for Developer: {} ", checkInReportRow.getDeveloperName(), e);
                            }
                        });
                        attestationReportsByAcbId.put(0L, getSummarizedAttestationReportForAllAcbs(checkInReportRows, changeRequestsByDeveloperId));
                        attestationReportsByAcbId.entrySet().forEach(entry -> attestationReportDAO.insert(entry.getValue()));
                    } else {
                        LOGGER.info("Not within submission plus approval window");
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                }
        });
        LOGGER.info("********* Completed Attestation Report Creator job. *********");
    }

    private void updateCountsBasedOnChangeRequestStatus(CheckInReport checkinReportRow, AttestationReport report) {
        if (checkinReportRow.getMostRecentAttestationChangeRequest() == null) {
               report.setNoSubmissionCount(report.getNoSubmissionCount() + 1);
               report.getDevelopersWithNoSubmissionAttestations().add(DeveloperAttestationStatus.builder()
                       .developer(checkinReportRow.getDeveloper())
                       .build());
       } else {

           switch (ChangeRequestStatusConcept.findByName(checkinReportRow.getMostRecentAttestationChangeRequest().getCurrentStatus().getChangeRequestStatusType().getName())) {
           case ACCEPTED:
               report.setApprovedCount(report.getApprovedCount() + 1);
               report.getDevelopersWithApprovedAttestations().add(DeveloperAttestationStatus.builder()
                       .developer(checkinReportRow.getDeveloper())
                       .changeRequestStatus(checkinReportRow.getMostRecentAttestationChangeRequest().getCurrentStatus())
                       .build());
               break;
           case PENDING_DEVELOPER_ACTION:
               report.setPendingDeveloperActionCount(report.getPendingDeveloperActionCount() + 1);
               report.getDeveloperWithPendingDeveloperActionAttestations().add(DeveloperAttestationStatus.builder()
                       .developer(checkinReportRow.getDeveloper())
                       .changeRequestStatus(checkinReportRow.getMostRecentAttestationChangeRequest().getCurrentStatus())
                       .build());
               break;
           case PENDING_ONC_ACB_ACTION:
               report.setPendingAcbActionCount(report.getPendingAcbActionCount() + 1);
               report.getDevelopersWithPendingAcbActionAttestations().add(DeveloperAttestationStatus.builder()
                       .developer(checkinReportRow.getDeveloper())
                       .changeRequestStatus(checkinReportRow.getMostRecentAttestationChangeRequest().getCurrentStatus())
                       .build());
               break;
           default:
               break;
           }
       }
    }

    private AttestationReport getSummarizedAttestationReportForAllAcbs(List<CheckInReport> checkInReportRows, Map<Long, ChangeRequest> changeRequestsByDeveloperId) {
        AttestationReport attestationReportForAllAcbs = AttestationReport.builder()
                .attestationPeriod(attestationPeriodService.getMostRecentPastAttestationPeriod())
                .certificationBody(CertificationBody.builder()
                        .id(0L)
                        .name("All ONC-ACBs")
                        .build())
                .reportDate(LocalDate.now())
                .developerCount(Long.valueOf(checkInReportRows.size()))
                .build();

        checkInReportRows.forEach(checkInReportRow -> {
            updateCountsBasedOnChangeRequestStatus(checkInReportRow, attestationReportForAllAcbs);
        });

        return attestationReportForAllAcbs;
    }

    private Boolean isDeveloperManagedByAcb(CheckInReport checkInReport, CertificationBody certificationBody) {
        return checkInReport.getCertificationBodies().stream()
                .filter(acb -> acb.getId().equals(certificationBody.getId()))
                .findAny()
                .isPresent();
    }
}
