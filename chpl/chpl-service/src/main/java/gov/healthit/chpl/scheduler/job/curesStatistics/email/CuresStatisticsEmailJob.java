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

import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.scheduler.job.curesStatistics.email.spreadsheet.CuresStatisticsChartSpreadsheet;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CuresStatisticsEmailJob  extends QuartzJob {
    @Autowired
    private CriterionListingStatisticsCsvCreator criterionListingStatisticsCsvCreator;

    @Autowired
    private OriginalCriterionUpgradedStatisticsCsvCreator originalCriterionUpgradedStatisticsCsvCreator;

    @Autowired
    private CuresCriterionUpgradedWithoutOriginalStatisticsCsvCreator curesCriterionUpgradedWithoutOriginalStatisticsCsvCreator;

    @Autowired
    private ListingCriterionForCuresAchievementStatisticsCsvCreator listingCriterionForCuresAchievementStatisticsCsvCreator;

    @Autowired
    private ListingCuresStatusStatisticsHtmlCreator listingCuresStatusStatisticsHtmlCreator;

    @Autowired
    private PrivacyAndSecurityListingStatisticsHtmlCreator privacyAndSecurityListingStatisticsHtmlCreator;

    @Autowired
    private CuresStatisticsChartSpreadsheet curesStatisticsChartSpreadsheet;

    @Autowired
    private CuresStatisticsChartData curesStatisticsChartData;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Email Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        List<File> attachments = new ArrayList<File>();
        try {
            File statisticsCsv = criterionListingStatisticsCsvCreator.createCsvFile();
            if (statisticsCsv != null) {
                attachments.add(statisticsCsv);
            }
        } catch (IOException ex) {
            LOGGER.error("Error creating statistics", ex);
        }
        try {
            File statisticsCsv = originalCriterionUpgradedStatisticsCsvCreator.createCsvFile();
            if (statisticsCsv != null) {
                attachments.add(statisticsCsv);
            }
        } catch (IOException ex) {
            LOGGER.error("Error creating statistics", ex);
        }
        try {
            File statisticsCsv = curesCriterionUpgradedWithoutOriginalStatisticsCsvCreator.createCsvFile();
            if (statisticsCsv != null) {
                attachments.add(statisticsCsv);
            }
        } catch (IOException ex) {
            LOGGER.error("Error creating statistics", ex);
        }
        try {
            File statisticsCsv = listingCriterionForCuresAchievementStatisticsCsvCreator.createCsvFile();
            if (statisticsCsv != null) {
                attachments.add(statisticsCsv);
            }
        } catch (IOException ex) {
            LOGGER.error("Error creating statistics", ex);
        }

        try {
            LocalDate reportDate = curesStatisticsChartData.getReportDate();
            attachments.add(
                curesStatisticsChartSpreadsheet.generateSpreadsheet(
                        curesStatisticsChartData.getCuresCriterionChartStatistics(reportDate), reportDate));
        } catch (IOException ex) {
            LOGGER.error("Error creating charts spreadhseet", ex);
        }

        try {
            sendEmail(context, attachments);
        } catch (EmailNotSentException ex) {
            LOGGER.error("Error sending email!", ex);
        }

        LOGGER.info("*****Cures Reporting Email Job is complete.*****");
    }

    private String createEmailBody() {
        String emailBody = "<h2>Cures Upgrade Statistics</h4><br/>";
        emailBody += listingCuresStatusStatisticsHtmlCreator.createEmailBody();
        emailBody += privacyAndSecurityListingStatisticsHtmlCreator.createEmailBody();
        return emailBody;
    }

    private void sendEmail(JobExecutionContext context, List<File> attachments) throws EmailNotSentException {
        String emailAddress = context.getMergedJobDataMap().getString(JOB_DATA_KEY_EMAIL);
        LOGGER.info("Sending email to: " + emailAddress);
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(emailAddress)
                .subject(env.getProperty("curesStatisticsReport.subject"))
                .htmlMessage(createEmailBody())
                .fileAttachments(attachments)
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + emailAddress);
    }

}
