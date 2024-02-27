package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UpdatedCriteriaStatusReportEmailJob extends QuartzJob {

    @Autowired
    private UpdatedCriteriaStatusReportWorkbook updatedCriteriaStatusReportWorkbook;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private JpaTransactionManager txManager;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("*****Updated Criteria Status Reporting Email Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        List<File> attachments = new ArrayList<File>();
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
                            attachments.add(updatedCriteriaStatusReportWorkbook.generateSpreadsheet());

                            sendEmail(context, attachments);
                        } catch (IOException ex) {
                            LOGGER.error("Error creating charts spreadhseet", ex);
                        } catch (EmailNotSentException ex) {
                            LOGGER.error("Error sending email!", ex);
                        }
                }
            });
        } catch (Exception ex) {
            LOGGER.error(ex);
        }
        LOGGER.info("*****Updated Criteria Status Reporting Email Job is complete.*****");
    }

    private void sendEmail(JobExecutionContext context, List<File> attachments) throws EmailNotSentException {
        String emailAddress = context.getMergedJobDataMap().getString(JOB_DATA_KEY_EMAIL);
        LOGGER.info("Sending email to: " + emailAddress);
        chplEmailFactory.emailBuilder()
                .recipient(emailAddress)
                .subject("TEST")
                .htmlMessage("TEST")
                .fileAttachments(attachments)
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + emailAddress);
    }

}
