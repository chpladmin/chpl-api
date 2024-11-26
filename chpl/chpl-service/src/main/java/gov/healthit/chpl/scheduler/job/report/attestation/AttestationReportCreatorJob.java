package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.manager.AttestationManager;
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.attestation.service.AttestationCertificationBodyService;
import gov.healthit.chpl.changerequest.dao.ChangeRequestAttestationDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.developer.attestation.DeveloperAttestationPeriodCalculator;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AttestationReportCreatorJob extends QuartzJob {
    private static final Integer DAYS_IN_APPROVAL_PERIOD = 30;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private AttestationPeriodService attestationPeriodService;

    @Autowired
    private AttestationManager attestationManager;

    @Autowired
    private DeveloperAttestationPeriodCalculator developerAttestationPeriodCalculator;

    @Autowired
    private CertificationBodyManager certificationBodyManager;

    @Autowired
    private AttestationReportDAO attestationReportDAO;

    @Autowired
    private ChangeRequestAttestationDAO changeRequestAttestationDAO;

    @Autowired
    private ChangeRequestManager changeRequestManager;

    @Autowired
    private AttestationCertificationBodyService attestationCertificationBodyService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Attestation Report Creator job. *********");
        // We need to manually create a transaction in this case because of how
        // AOP works. When a method is annotated with @Transactional, the transaction
        // wrapper is only added if the object's proxy is called. The object's proxy is
        // not called when the method is called from within this class. The object's
        // proxy is called when the method is public and is called from a different
        // object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {

                try {
                    if (inSubmissionPlusApprovalPeriod()) {
                        if (!CollectionUtils.isEmpty(attestationReportDAO.getAttestationReportByDate(LocalDate.now()))) {
                            attestationReportDAO.deleteAttestationReportByDate(LocalDate.now());
                        }

                        AttestationPeriod mostRecentPastAttestationPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
                        Map<Long, AttestationReport> attestationReportsByAcbId = new HashMap<Long, AttestationReport>();
                        List<CertificationBody> activeAcbs = certificationBodyManager.getAllActive();
                        List<Developer> applicableDevelopers = getDevelopersActiveListingsDuringMostRecentPastAttestationPeriod();

                        applicableDevelopers.forEach(developer -> {
                            LOGGER.info("Processing Developer: {} ({})", developer.getName(), developer.getId());
                            try {

                                ChangeRequest cr = getMostRecentChangeRequest(developer, mostRecentPastAttestationPeriod);

                                activeAcbs.forEach(acb -> {
                                   if (isDeveloperManagedByAcb(developer, acb, mostRecentPastAttestationPeriod)) {
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

                                       if (cr == null) {
                                           report.setNoSubmissionCount(report.getNoSubmissionCount() + 1);
                                       } else {
                                           switch (cr.getCurrentStatus().getChangeRequestStatusType().getName()) {
                                           case "Accepted":
                                               report.setApprovedCount(report.getApprovedCount() + 1);
                                               break;
                                           case "Pending Developer Action":
                                               report.setPendingDeveloperActionCount(report.getPendingDeveloperActionCount() + 1);
                                               break;
                                           case "Pending ONC-ACB Action":
                                               report.setPendingAcbActionCount(report.getPendingAcbActionCount() + 1);
                                               break;
                                           default:
                                               break;
                                           }
                                       }
                                   }
                                });
                            } catch (Exception e) {
                                LOGGER.error("Could not collect Developer Attestation Report data for Developer: {} ", developer.getName(), e);
                            }
                        });

                        attestationReportsByAcbId.entrySet().forEach(entry -> attestationReportDAO.insert(entry.getValue()));

                    } else {
                        LOGGER.info("Not within submission plus approval window");
                    }
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        });
        LOGGER.info("********* Completed Attestation Report Creator job. *********");
    }

    private boolean inSubmissionPlusApprovalPeriod() {
        AttestationPeriod attestationPeriod = attestationPeriodService.getMostRecentPastAttestationPeriod();
        return DateUtil.isDateBetweenInclusive(
                Pair.of(attestationPeriod.getSubmissionStart(), attestationPeriod.getSubmissionEnd().plusDays(DAYS_IN_APPROVAL_PERIOD)),
                LocalDate.now());
    }

    private List<CertificationBody> getActiveAcbs() {
        return certificationBodyManager.getAllActive();
    }

    private List<Developer> getDevelopersActiveListingsDuringMostRecentPastAttestationPeriod() {
        AttestationPeriod mostRecentPastPeriod = attestationManager.getMostRecentPastAttestationPeriod();
        return developerAttestationPeriodCalculator.getDevelopersWithActiveListingsDuringAttestationPeriod(mostRecentPastPeriod, LOGGER);
    }

    private ChangeRequest getMostRecentChangeRequest(Developer developer, AttestationPeriod period) {
        Long crId = changeRequestAttestationDAO.getIdOfMostRecentAttestationChangeRequest(developer.getId(), period.getId());
        if (crId == null) {
            LOGGER.warn("No change request was found for developer " + developer.getId() + " and attestation period " + period.getId());
            return null;
        }
        ChangeRequest changeRequest = null;
        try {
            changeRequest = changeRequestManager.getChangeRequest(crId);
        } catch (Exception ex) {
            LOGGER.error("Error getting change request with ID " + crId, ex);
        }
        return changeRequest;
    }

    private Boolean isDeveloperManagedByAcb(Developer developer, CertificationBody certificationBody, AttestationPeriod attestationPeriod) {
        return attestationCertificationBodyService.getAssociatedCertificationBodies(developer.getId(), attestationPeriod.getId()).stream()
                .filter(acb -> acb.getId().equals(certificationBody.getId()))
                .findAny()
                .isPresent();
    }
}
