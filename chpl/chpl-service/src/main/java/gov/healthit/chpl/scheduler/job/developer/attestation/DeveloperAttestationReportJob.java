package gov.healthit.chpl.scheduler.job.developer.attestation;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.ff4j.FF4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertificationBodyDAO;
import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.SchedulerManager;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "developerAttestationReportJobLogger")
public class DeveloperAttestationReportJob implements Job {

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private DeveloperAttestationReportDataCollection developerAttestationReportDataCollection;

    @Autowired
    private DeveloperAttestationReportCsvWriter developerAttestationReportCsvWriter;

    @Autowired
    private CertificationBodyDAO certificationBodyDAO;

    @Autowired
    private FF4j ff4j;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Value("${developer.attestation.report.subject}")
    private String emailSubject;

    @Value("${developer.attestation.report.body}")
    private String emailBody;

    @Value("${chpl.email.greeting}")
    private String chplEmailGreeting;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Attestation Report job. *********");

        // We need to manually create a transaction in this case because of how AOP works. When a method is
        // annotated with @Transactional, the transaction wrapper is only added if the object's proxy is called.
        // The object's proxy is not called when the method is called from within this class. The object's proxy
        // is called when the method is public and is called from a different object.
        // https://stackoverflow.com/questions/3037006/starting-new-transaction-in-spring-bean
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {

            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    if (!ff4j.check(FeatureList.ATTESTATIONS)) {
                        LOGGER.info("Sending report not implemented email to: {}", context.getMergedJobDataMap().getString("email"));
                        sendNotImplementedEmail(context);

                    } else {
                        List<DeveloperAttestationReport> reportRows = developerAttestationReportDataCollection.collect(getAcbIds(context));
                        File csv = developerAttestationReportCsvWriter.generateFile(reportRows);
                        chplEmailFactory.emailBuilder()
                                .recipient(context.getMergedJobDataMap().getString("email"))
                                .subject(emailSubject)
                                .fileAttachments(Arrays.asList(csv))
                                .htmlMessage(chplHtmlEmailBuilder.initialize()
                                        .heading(emailSubject)
                                        .paragraph(String.format(emailBody), getAcbNamesAsBrSeparatedList(context))
                                        .footer(true)
                                        .build())
                                .sendEmail();
                        LOGGER.info("Report sent to: {}", context.getMergedJobDataMap().getString("email"));
                    }
                } catch (Exception e) {
                    LOGGER.catching(e);
                }
            }
        });
        LOGGER.info("********* Completed Developer Attestation Report job. *********");
    }

    private void sendNotImplementedEmail(JobExecutionContext context) throws EmailNotSentException {
        chplEmailFactory.emailBuilder()
                .recipient(context.getMergedJobDataMap().getString("email"))
                .subject(emailSubject)
                .htmlMessage(chplHtmlEmailBuilder.initialize()
                        .heading(emailSubject)
                        .paragraph("", "This report is not yet implemented.")
                        .footer(true)
                        .build())
                .sendEmail();
    }

    private List<Long> getAcbIds(JobExecutionContext context) {
        return Arrays.asList(context.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                .map(acb -> Long.parseLong(acb))
                .collect(Collectors.toList());
    }

    private String getAcbNamesAsBrSeparatedList(JobExecutionContext jobContext) {
        if (Objects.nonNull(jobContext.getMergedJobDataMap().getString("acb"))) {
            return Arrays.asList(
                    jobContext.getMergedJobDataMap().getString("acb").split(SchedulerManager.DATA_DELIMITER)).stream()
                    .map(acbId -> getAcbName(Long.valueOf(acbId)))
                    .collect(Collectors.joining("<br />"));
        } else {
            return "";
        }
    }

    private String getAcbName(Long acbId) {
        try {
            return certificationBodyDAO.getById(acbId).getName();
        } catch (NumberFormatException | EntityRetrievalException e) {
            LOGGER.error("Could not retreive ACB name based on value: " + acbId, e);
            return "";
        }
    }

}
