package gov.healthit.chpl.scheduler.job.curesStatistics;

import java.util.Collections;

import javax.mail.MessagingException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.statistics.CriterionListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.CriterionUpgradedToCuresFromOriginalListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingCuresStatusStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingToCriterionForCuresAchievementStatisticsDAO;
import gov.healthit.chpl.dao.statistics.PrivacyAndSecurityListingStatisticsDAO;
import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.EmailBuilder;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesStatisticsEmailJobLogger")
public class CuresStatisticsEmailJob  extends QuartzJob {
    @Autowired
    private CriterionListingStatisticsDAO criterionListingStatisticsDao;

    @Autowired
    private CuresCriterionUpgradedWithoutOriginalListingStatisticsDAO curesCriterionUpgradedWithoutOriginalStatisticDao;

    @Autowired
    private CriterionUpgradedToCuresFromOriginalListingStatisticsDAO criterionUpgradedToCuresFromOriginalStatisticsDao;

    @Autowired
    private ListingCuresStatusStatisticsDAO listingCuresStatusStatisticsDao;

    @Autowired
    private PrivacyAndSecurityListingStatisticsDAO privacyAndSecurityListingStatisticsDao;

    @Autowired
    private ListingToCriterionForCuresAchievementStatisticsDAO listingToCuresAchievementDao;

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        LOGGER.info("*****Cures Reporting Email Job is starting.*****");
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        try {
            sendEmail(context);
        } catch (MessagingException ex) {
            LOGGER.error("Error sending email!", ex);
        }

        LOGGER.info("*****Cures Reporting Email Job is complete.*****");
    }

    private void sendEmail(JobExecutionContext context) throws MessagingException {
        String emailAddress = context.getMergedJobDataMap().getString(JOB_DATA_KEY_EMAIL);
        LOGGER.info("Sending email to: " + emailAddress);
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(emailAddress)
                .subject(env.getProperty("curesStatisticsReport.subject"))
                .htmlMessage("") //TODO:
                .fileAttachments(Collections.emptyList()) //TODO:
                .sendEmail();
        LOGGER.info("Completed Sending email to: " + emailAddress);
    }
}
