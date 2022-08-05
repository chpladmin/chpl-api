package gov.healthit.chpl.scheduler.job.developer.attestation;

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

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "developerAttestationCheckinReportJobLogger")
public class DeveloperAttestationCheckInReportJob implements Job {

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private DeveloperAttestationCheckInReportDataCollector developerAttestationCheckInReportDataCollection;

    @Autowired
    private DeveloperAttestationCheckInReportCsvWriter developerAttestationCheckInReportCsvWriter;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Value("${developer.attestation.checkin.report.subject}")
    private String emailSubject;

    @Value("${developer.attestation.checkin.report.body}")
    private String emailBody;

    @Value("${developer.attestation.checkin.report.sectionHeading}")
    private String sectionHeading;

    @Value("${chpl.email.greeting}")
    private String chplEmailGreeting;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting Developer Attestation Check-in Report job. *********");

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
                    //List<DeveloperAttestationCheckInReport> reportRows = developerAttestationCheckInReportDataCollection.collect();
                    //File csv = developerAttestationCheckInReportCsvWriter.generateFile(reportRows);
                    chplEmailFactory.emailBuilder()
                            .recipient(context.getMergedJobDataMap().getString("email"))
                            .subject(emailSubject)
                            //.fileAttachments(Arrays.asList(csv))
                            .htmlMessage(chplHtmlEmailBuilder.initialize()
                                    .heading(sectionHeading)
                                    .paragraph("", emailBody)
                                    .footer(true)
                                    .build())
                            .sendEmail();
                    LOGGER.info("Report sent to: {}", context.getMergedJobDataMap().getString("email"));
                } catch (Exception e) {
                    LOGGER.catching(e);
                }
            }
        });
        LOGGER.info("********* Completed Developer Attestation Check-in Report job. *********");
    }
}
