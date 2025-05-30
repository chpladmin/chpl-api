package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Arrays;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.email.footer.AdminFooter;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "updatedCriteriaStatusReportEmailJobLogger")
public class UpdatedCriteriaStatusReportEmailJob extends QuartzJob {

    @Autowired
    private ReportDateService reportDateService;

    @Autowired
    private UpdatedCriteriaStatusReportCsvCreator updatedCriteriaStatusReportCsvCreator;

    @Autowired
    private UpdatedCriteriaStatusReportWorkbook updatedCriteriaStatusReportWorkbookCreator;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("*****Updated Criteria Status Reporting Email Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        try {
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
                            sendEmail(context);
                        } catch (IOException ex) {
                            LOGGER.error("Error creating email body", ex);
                        } catch (EmailNotSentException ex) {
                            LOGGER.error("Error sending email!", ex);
                        } catch (ValidationException ex) {
                            LOGGER.error("Error searching for active listings!", ex);
                        }
                }
            });
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
        LOGGER.info("*****Updated Criteria Status Reporting Email Job is complete.*****");
    }

    private void sendEmail(JobExecutionContext context) throws EmailNotSentException, IOException, ValidationException {
        String emailAddress = context.getMergedJobDataMap().getString(JOB_DATA_KEY_EMAIL);
        LOGGER.info("Sending email to: " + emailAddress);
        chplEmailFactory.emailBuilder()
                .recipient(emailAddress)
                .subject(env.getProperty("updatedCriteriaStatusReport.subject"))
                .htmlMessage(createHtmlMessage())
                .fileAttachments(Arrays.asList(
                        updatedCriteriaStatusReportCsvCreator.createCsvFile(),
                        updatedCriteriaStatusReportWorkbookCreator.generateSpreadsheet()
                        ))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + emailAddress);
    }

    private String createHtmlMessage() throws ValidationException {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("updatedCriteriaStatusReport.subject"))
                .paragraph("", getHtmlEmailBody())
                .footer(AdminFooter.class)
                .build();
    }

    private String getHtmlEmailBody() throws ValidationException {
        return String.format(env.getProperty("updatedCriteriaStatusReport.body"),
                getReportDate().toString());
    }

    private LocalDate getReportDate() {
        return reportDateService.findClosestDateWithSummaryStatisticsAndUpdatedCriterionStatusData(LocalDate.now());
    }
}
