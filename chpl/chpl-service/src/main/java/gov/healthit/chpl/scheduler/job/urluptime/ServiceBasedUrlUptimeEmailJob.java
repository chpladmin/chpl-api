package gov.healthit.chpl.scheduler.job.urluptime;

import java.io.IOException;
import java.util.Arrays;
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
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBasedUrlUptimeEmailJobLogger")
public class ServiceBasedUrlUptimeEmailJob extends QuartzJob {

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Autowired
    private ServiceBasedUrlUptimeCalculator urlUptimeCalculator;

    @Autowired
    private ServiceBasedUrlUptimeCsvWriter urlUptimeCsvWriter;

    @Autowired
    private JpaTransactionManager txManager;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Url Uptime Email job *********");
        TransactionTemplate txTemplate = new TransactionTemplate(txManager);
        txTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        txTemplate.execute(new TransactionCallbackWithoutResult() {
            @Override
            protected void doInTransactionWithoutResult(TransactionStatus status) {
                try {
                    sendEmail(context, getReportRows());
                } catch (Exception e) {
                    LOGGER.error(e);
                }
            }
        });
        LOGGER.info("********* Completed the Url Uptime Email job *********");
    }

    private List<ServiceBasedUrlUptimeReport> getReportRows() {
        return urlUptimeCalculator.calculateRowsForReport();
    }

    private void sendEmail(JobExecutionContext context, List<ServiceBasedUrlUptimeReport> rows) throws EmailNotSentException, IOException {
        LOGGER.info("Sending email to: " + context.getMergedJobDataMap().getString("email"));
        chplEmailFactory.emailBuilder()
                .recipient(context.getMergedJobDataMap().getString("email"))
                .subject(env.getProperty("serviceBasedUrlUptime.report.subject"))
                .htmlMessage(createHtmlMessage(context, rows.size()))
                .fileAttachments(Arrays.asList(urlUptimeCsvWriter.generateFile(rows)))
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + context.getMergedJobDataMap().getString("email"));
    }

    private String createHtmlMessage(JobExecutionContext context, int errorCount) {
        return chplHtmlEmailBuilder.initialize()
                .heading(env.getProperty("serviceBasedUrlUptime.report.subject"))
                .paragraph("", env.getProperty("serviceBasedUrlUptime.report.paragraph1.body"))
                .footer(true)
                .build();
    }
}
