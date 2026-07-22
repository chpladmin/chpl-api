package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionOperations;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.scheduler.SchedulerSecurityContextService;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "developerAttestationCheckinReportJobLogger")
public class DeveloperAttestationCheckInReportJob extends QuartzJob {

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    private AttestationCheckinReportDAO attestationCheckinReportDAO;

    @Autowired
    private CheckInReportSummaryDataCollector checkInReportSummaryDataCollector;

    @Autowired
    private CheckInReportRwtResultsDataCollector checkInReportRwtResultsDataCollector;

    @Autowired
    private CheckInReportCsvWriter checkInReportCsvWriter;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private SchedulerSecurityContextService securityContextService;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Value("${developer.attestation.checkin.report.subject}")
    private String emailSubject;

    @Value("${developer.attestation.checkin.report.body}")
    private String emailBody;

    @Value("${developer.attestation.checkin.report.body2}")
    private String emailBody2;

    @Value("${developer.attestation.checkin.report.body3}")
    private String emailBody3;

    @Value("${developer.attestation.checkin.report.sectionHeading}")
    private String sectionHeading;

    @Value("${chpl.email.greeting}")
    private String chplEmailGreeting;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Attestation Check-in Report job. *********");

        // We need to manually create a transaction in this case because of how
        // AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added
        // if the object's proxy is called.
        // The object's proxy is not called when the method is called from
        // within this class. The object's proxy
        // is called when the method is public and is called from a different
        // object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionOperations transactionOperations = new TransactionTemplate(transactionManager,
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        transactionOperations.executeWithoutResult(status -> {
                try {
                    LOGGER.info("Starting the transaction");
                    securityContextService.setAdminSecurityContext();
                    LOGGER.info("Set the Security Context");

                    List<CertificationBody> acbs = getAcbIds(context).stream()
                            .map(acbId -> getCertificationBody(acbId))
                            .toList();

                    List<CheckInReport> reportRows = getCheckInReports(acbs);

                    CheckInReportSummary reportSummary = checkInReportSummaryDataCollector.collect(reportRows);
                    File csv = checkInReportCsvWriter.generateFile(reportRows);
                    chplEmailFactory.emailBuilder()
                            .recipient(context.getMergedJobDataMap().getString("email"))
                            .subject(emailSubject)
                            .fileAttachments(Arrays.asList(csv))
                            .htmlMessage(chplHtmlEmailBuilder.initialize()
                                    .heading(sectionHeading)
                                    .paragraph("", emailBody)
                                    .paragraph("", String.format(emailBody2,
                                            reportSummary.getDeveloperCount(),
                                            reportSummary.doCountsEqualDeveloperCount() ? "" : "*",
                                            reportSummary.getAttestationsApprovedCount(),
                                            reportSummary.getPendingAcbActionCount(),
                                            reportSummary.getPendingDeveloperActionCount(),
                                            reportSummary.getNoSubmissionCount()))
                                    .paragraph("", reportSummary.doCountsEqualDeveloperCount() ? "" : emailBody3)
                                    .footer(AdminFooter.class)
                                    .build())
                            .sendEmail();
                    LOGGER.info("Report sent to: {}", context.getMergedJobDataMap().getString("email"));
                } catch (Exception e) {
                    LOGGER.catching(e);
                }
        });
        LOGGER.info("********* Completed Developer Attestation Check-in Report job. *********");
    }

    private List<CheckInReport> getCheckInReports(List<CertificationBody> acbs) {
        return attestationCheckinReportDAO.getCheckinReports(attestationCheckinReportDAO.getMaxReportDate()).stream()
                .filter(cr -> isCheckinreportValidOForAcbs(cr, acbs))
                .peek(checkInReport -> checkInReport.setCriterionAndSvapData(checkInReportRwtResultsDataCollector.collect(checkInReport.getDeveloperId())))
                .collect(Collectors.toList());
    }

    private boolean isCheckinreportValidOForAcbs(CheckInReport report, List<CertificationBody> acbs) {
        List<String> acbNames = acbs.stream()
                .map(acb -> acb.getName())
                .collect(Collectors.toList());

        return Stream.of(report.getRelevantAcbs().split(";"))
                .filter(acbNames::contains)
                .findAny()
                .isPresent();
    }

    private List<Long> getAcbIds(JobExecutionContext context) {
        return Arrays.asList(context.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acb -> Long.parseLong(acb))
                .collect(Collectors.toList());
    }

    private CertificationBody getCertificationBody(Long id) {
        try {
            return certificationBodyDAO.getById(id);
        } catch (EntityRetrievalException e) {
            LOGGER.error("Unable to retrieve certification body with id: {}", id, e);
            return null;
        }
    }
}
