package gov.healthit.chpl.scheduler.job.curesStatistics.email;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.email.ChplHtmlEmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet.CuresChartsOverTimeSpreadheet;
import gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet.CuresStatisticsChartSpreadsheet;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CuresStatisticsEmailJob  extends QuartzJob {
    @Autowired
    private CuresStatisticsChartSpreadsheet curesStatisticsChartSpreadsheet;

    @Autowired
    private CuresChartsOverTimeSpreadheet curesChartsOverTimeSpreadsheet;

    @Autowired
    private CuresStatisticsChartData curesStatisticsChartData;

    @Autowired
    private ChplHtmlEmailBuilder chplHtmlEmailBuilder;

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Email Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        List<File> attachments = new ArrayList<File>();
        try {
            LocalDate reportDate = curesStatisticsChartData.getReportDate();
            attachments.add(curesStatisticsChartSpreadsheet.generateSpreadsheet(reportDate));
            attachments.add(curesChartsOverTimeSpreadsheet.generateSpreadsheet());
            sendEmail(context, attachments);
        } catch (IOException ex) {
            LOGGER.error("Error creating charts spreadhseet", ex);
        } catch (EmailNotSentException ex) {
            LOGGER.error("Error sending email!", ex);
        }

        LOGGER.info("*****Cures Reporting Email Job is complete.*****");
    }

    private void sendEmail(JobExecutionContext context, List<File> attachments) throws EmailNotSentException {
        String emailAddress = context.getMergedJobDataMap().getString(JOB_DATA_KEY_EMAIL);
        LOGGER.info("Sending email to: " + emailAddress);
        chplEmailFactory.emailBuilder()
                .recipient(emailAddress)
                .subject(env.getProperty("curesStatisticsReport.subject"))
                .htmlMessage(createHtmlMessage())
                .fileAttachments(attachments)
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + emailAddress);
    }

    private String createHtmlMessage() {
        return chplHtmlEmailBuilder.initialize()
                .heading("Cures Upgrade Statistics")
                .paragraph("", env.getProperty("curesStatisticsReport.listingCuresStatusStatistics.body"))
                .footer(true)
                .build();
    }
}
