package gov.healthit.chpl.scheduler.job;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

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

/**
 * Job run by Scheduler to send email when an ONC-ACB did something that might trigger a Developer Ban.
 * @author alarned
 *
 */
public class TriggerDeveloperBanJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("triggerDeveloperBanJobLogger");
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private Properties properties = null;

    @Autowired
    private Environment env;

    /**
     * Default constructor.
     * @throws IOException if unable to load properties
     */
    public TriggerDeveloperBanJob() throws IOException {
        InputStream in = TriggerDeveloperBanJob.class.getClassLoader()
                .getResourceAsStream(DEFAULT_PROPERTIES_FILE);
        if (in == null) {
            properties = null;
            throw new FileNotFoundException("Environment Properties File not found in class path.");
        } else {
            properties = new Properties();
            properties.load(in);
            in.close();
        }
    }

    /**
     * Main method. Sends email messages to subscribers of that notification.
     * @param jobContext for context of the job
     * @throws JobExecutionException if necessary
     */
    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Trigger Developer Ban job. *********");

        String[] recipients = jobContext.getMergedJobDataMap().getString("email").split("\u263A");

        try {
            sendEmail(jobContext, recipients);
        } catch (IOException | MessagingException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Trigger Developer Ban job. *********");
    }

    private void sendEmail(final JobExecutionContext jobContext, final String[] recipients)
            throws IOException, AddressException, MessagingException {

        String subject = "NEED TO REVIEW: Certification Status of listing set to \""
                + jobContext.getMergedJobDataMap().getString("status") + "\"";
        String htmlMessage = createHtmlEmailBody(jobContext);

        LOGGER.info("Sending email to: " + jobContext.getMergedJobDataMap().getString("email"));
        LOGGER.info("Message to be sent: " + htmlMessage);

        List<String> addresses = Arrays.asList(recipients);

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(addresses)
        .subject(subject)
        .htmlMessage(htmlMessage)
        .sendEmail();
    }

    private String createHtmlEmailBody(final JobExecutionContext jobContext) {
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
        String htmlMessage = String.format("<p>The CHPL Listing <a href=\"%s/#/product/%d\">%s</a>, owned by \"%s\" "
                + "and certified by \"%s\" has been set on \"%s\" by \"%s\" to a Certification Status of \"%s\" with "
                + "an effective date of \"%s\".</p>"
                + "<p>%s</p>"
                + "<p>%s</p>"
                + "<p>There %s %d Open Nonconformit%s and %d Closed Nonconformit%s.</p>"
                + "<p>ONC should review the activity and all details of the listing to determine if "
                + "this action warrants a ban on the Developer.</p>",
                properties.getProperty("chplUrlBegin"),                                 // root of URL
                jdm.getLong("dbId"),                                                    // for URL to product page
                jdm.getString("chplId"),                                                // visible link
                jdm.getString("developer"),                                             // developer name
                jdm.getString("acb"),                                                   // ACB name
                Util.getDateFormatter().format(new Date(jdm.getLong("changeDate"))),    // date of change
                jdm.getString("fullName"),                                              // user making change
                jdm.getString("status"),                                                // target status
                Util.getDateFormatter().format(new Date(jdm.getLong("effectiveDate"))), // effective date of change
                reasonForStatusChange,                                                  // reason for change
                reasonForListingChange,                                                 // reason for change
                (openNcs != 1 ? "were" : "was"), openNcs, (openNcs != 1 ? "ies" : "y"), // formatted counts of open
                closedNcs, (closedNcs != 1 ? "ies" : "y"));    // and closed nonconformities, with English word endings
        return htmlMessage;
    }
}
