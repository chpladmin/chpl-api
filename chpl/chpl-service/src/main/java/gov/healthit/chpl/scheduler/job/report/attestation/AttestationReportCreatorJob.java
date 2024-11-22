package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;
import java.util.List;

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
import gov.healthit.chpl.attestation.manager.AttestationPeriodService;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.manager.CertificationBodyManager;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.developer.attestation.CheckInReport;
import gov.healthit.chpl.scheduler.job.developer.attestation.CheckInReportDataCollector;
import gov.healthit.chpl.scheduler.job.developer.attestation.CheckInReportSummary;
import gov.healthit.chpl.scheduler.job.developer.attestation.CheckInReportSummaryDataCollector;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class AttestationReportCreatorJob extends QuartzJob{
    private static final Integer DAYS_IN_APPROVAL_PERIOD = 30;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private AttestationPeriodService attestationPeriodService;

    @Autowired
    private CheckInReportDataCollector checkInReportDataCollector;

    @Autowired
    private CheckInReportSummaryDataCollector checkInReportSummaryDataCollector;

    @Autowired
    private CertificationBodyManager certificationBodyManager;

    @Autowired
    private AttestationReportDAO attestationReportDAO;

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
                        List<CertificationBody> acbs = certificationBodyManager.getAllActive();

                        acbs.forEach(acb -> {
                            try {
                                List<CheckInReport> checkInReports = checkInReportDataCollector.collectWithoutDetails(List.of(acb.getId()));
                                CheckInReportSummary checkInReportSummary = checkInReportSummaryDataCollector.collect(checkInReports);
                                AttestationReport attestationReport = AttestationReport.builder()
                                        .attestationsApprovedCount(checkInReportSummary.getAttestationsApprovedCount())
                                        .developerCount(checkInReportSummary.getDeveloperCount())
                                        .noSubmissionCount(checkInReportSummary.getNoSubmissionCount())
                                        .pendingAcbActionCount(checkInReportSummary.getPendingAcbActionCount())
                                        .pendingDeveloperActionCount(checkInReportSummary.getPendingDeveloperActionCount())
                                        .build();


                                attestationReport.setAttestationPeriod(attestationPeriodService.getMostRecentPastAttestationPeriod());
                                attestationReport.setReportDate(LocalDate.now());
                                attestationReport.setCertificationBody(acb);


                                attestationReportDAO.insert(attestationReport);
                            } catch (Exception e) {
                                LOGGER.error("Could not collect Developer Attestation Report data for ONC-ACB: {} ", acb.getName(), e);
                            }
                        });
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
}
