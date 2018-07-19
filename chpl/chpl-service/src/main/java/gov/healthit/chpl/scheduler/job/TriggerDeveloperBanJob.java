package gov.healthit.chpl.scheduler.job;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import gov.healthit.chpl.auth.SendMailUtil;
import gov.healthit.chpl.util.Util;

/**
 * Job run by Scheduler to send email when an ONC-ACB did something that might trigger a Developer Ban.
 * @author alarned
 *
 */
public class TriggerDeveloperBanJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger(TriggerDeveloperBanJob.class);
    private static final String DEFAULT_PROPERTIES_FILE = "environment.properties";
    private Properties properties = null;

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
        String[] recipients = jobContext.getMergedJobDataMap().getString("email").split("\u263A");
        try {
            sendEmail(jobContext, recipients);
        } catch (IOException | MessagingException e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(final JobExecutionContext jobContext, final String[] recipients)
            throws IOException, AddressException, MessagingException {

        String subject = "NEED TO REVIEW: Certification Status of listing set to \""
                + jobContext.getMergedJobDataMap().getString("status") + "\"";
        String htmlMessage = createHtmlEmailBody(jobContext);

        LOGGER.info("Sending email to {} with subject {} and content {}",
                String.join(",", recipients), subject, htmlMessage);
        SendMailUtil mailUtil = new SendMailUtil();
        mailUtil.sendEmail(null, recipients, subject, htmlMessage, null, properties);
    }

    private String createHtmlEmailBody(final JobExecutionContext jobContext) {
        String url = properties.getProperty("chplUrlBegin");
        String htmlMessage = "<p>The CHPL Listing <a href=\"" + url + "/#/product/"
                + jobContext.getMergedJobDataMap().getLong("dbId") + "\">"
                + jobContext.getMergedJobDataMap().getString("chplId") + "</a>, owned by \""
                + jobContext.getMergedJobDataMap().getString("developer") + "\", and certified by \""
                + jobContext.getMergedJobDataMap().getString("acb") + "\" has been set on \""
                + Util.getDateFormatter().format(new Date(jobContext.getMergedJobDataMap().getLong("changeDate")))
                + "\" by \"" + jobContext.getMergedJobDataMap().getString("firstName") + " "
                + jobContext.getMergedJobDataMap().getString("lastName") + "\" to a Certification Status of \""
                + jobContext.getMergedJobDataMap().getString("status") + "\" with an effective date of \""
                + Util.getDateFormatter().format(new Date(jobContext.getMergedJobDataMap().getLong("effectiveDate")))
                + "\".</p><p>There " + (jobContext.getMergedJobDataMap().getInt("openNcs") != 1 ? "were" : "was")
                + " \"" + jobContext.getMergedJobDataMap().getInt("openNcs") + "\" Open Nonconformit"
                + (jobContext.getMergedJobDataMap().getInt("openNcs") != 1 ? "ies" : "y") + " and \""
                + jobContext.getMergedJobDataMap().getInt("closedNcs") + "\" Closed Nonconformit"
                + (jobContext.getMergedJobDataMap().getInt("closedNcs") != 1 ? "ies" : "y") + ".</p><p>"
                + "ONC should review the activity and all details of the listing to determine if it warrants a ban "
                + "on the Developer.</p>";
        return htmlMessage;
    }
}
