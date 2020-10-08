package gov.healthit.chpl.scheduler.job;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.Util;

public class TriggerDeveloperBanJob implements Job {
    private static Logger LOGGER = LogManager.getLogger("triggerDeveloperBanJobLogger");

    @Autowired
    private Environment env;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Trigger Developer Ban job. *********");

        String[] recipients = jobContext.getMergedJobDataMap().getString("email").split("\u263A");

        try {
            sendEmails(jobContext, recipients);
        } catch (IOException | MessagingException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Trigger Developer Ban job. *********");
    }

    private void sendEmails(JobExecutionContext jobContext, String[] recipients)
            throws IOException, AddressException, MessagingException {

        String subject = "NEED TO REVIEW: Certification Status of listing set to \""
                + jobContext.getMergedJobDataMap().getString("status") + "\"";
        String htmlMessage = createHtmlEmailBody(jobContext);

        List<String> emailAddresses = Arrays.asList(recipients);
        for (String emailAddress : emailAddresses) {
            try {
                sendEmail(emailAddress, subject, htmlMessage);
            } catch (Exception ex) {
                LOGGER.error("Could not send message to " + emailAddress, ex);
            }
        }
    }

    private void sendEmail(String recipientEmail, String subject, String htmlMessage)
            throws MessagingException {
        LOGGER.info("Sending email to: " + recipientEmail);
        LOGGER.info("Message to be sent: " + htmlMessage);

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipient(recipientEmail)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
    }

    private String createHtmlEmailBody(JobExecutionContext jobContext) {
        JobDataMap jdm = jobContext.getMergedJobDataMap();
        String reasonForStatusChange = jdm.getString("reason");
        if (StringUtils.isEmpty(reasonForStatusChange)) {
            reasonForStatusChange = "<strong>ONC-ACB provided reason for status change:</strong> This field is blank";
        } else {
            reasonForStatusChange = "<strong>ONC-ACB provided reason for status change:</strong> \""
                    + reasonForStatusChange + "\"";
        }
        String reasonForListingChange = jdm.getString("reasonForChange");
        if (StringUtils.isEmpty(reasonForListingChange)) {
            reasonForListingChange = "<strong>ONC-ACB provided reason for listing change:</strong> This field is blank";
        } else {
            reasonForListingChange = "<strong>ONC-ACB provided reason for listing change:</strong> \""
                    + reasonForListingChange + "\"";
        }
        int openNcs = jdm.getInt("openNcs");
        int closedNcs = jdm.getInt("closedNcs");
        String htmlMessage = String.format(env.getProperty("developerBanEmailBody"),
                env.getProperty("chplUrlBegin"), // root of URL
                env.getProperty("listingDetailsUrl"),
                jdm.getLong("dbId"), // for URL to product page
                jdm.getString("chplId"), // visible link
                jdm.getString("developer"), // developer name
                jdm.getString("acb"), // ACB name
                Util.getDateFormatter().format(new Date(jdm.getLong("changeDate"))), // date of change
                jdm.getString("fullName"), // user making change
                jdm.getString("status"), // target status
                Util.getDateFormatter().format(new Date(jdm.getLong("effectiveDate"))), // effective date of change
                reasonForStatusChange, // reason for change
                reasonForListingChange, // reason for change
                (openNcs != 1 ? "were" : "was"), openNcs, (openNcs != 1 ? "ies" : "y"), // formatted counts of open
                closedNcs, (closedNcs != 1 ? "ies" : "y")); // and closed nonconformities, with English word endings
        return htmlMessage;
    }
}
