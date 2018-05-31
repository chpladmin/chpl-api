package gov.healthit.chpl.app.scheduler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.app.AppConfig;
import gov.healthit.chpl.app.NotificationEmailerReportApp;

/**
 * Job run by Scheduler to send email when the cache is "too old".
 * @author alarned
 *
 */
@Component("cacheStatusAgeApp")
public class CacheStatusAgeJob extends NotificationEmailerReportApp implements Job {
    private AbstractApplicationContext context;
    /**
     * Default constructor.
     * @throws Exception if context can't be configured
     */
    public CacheStatusAgeJob() throws Exception {
        this.setLocalContext();
        context = new AnnotationConfigApplicationContext(AppConfig.class);
        initiateSpringBeans(context);
    }

    /**
     * Main method. Checks to see if the cache is old, then, if it is,
     * sends email messages to subscribers of that notification.
     * @param jobContext for context of the job
     * @throws JobExecutionException if necessary
     */

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        try {
            if (isCacheOld()) {
                String recipient = jobContext.getMergedJobDataMap().getString("email");
                try {
                    sendEmail(recipient);
                } catch (IOException | MessagingException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        context.close();
    }

    private boolean isCacheOld() throws UnsupportedEncodingException, IOException {
        Properties props = getProperties();
        Pattern agePattern = Pattern.compile("\"age\": (\\d*)");
        URL statusUrl = new URL(props.getProperty("cacheStatusMaxAgeUrl"));
        InputStreamReader isr =  new InputStreamReader(statusUrl.openStream(), "UTF-8");
        BufferedReader in = new BufferedReader(isr);

        try {
            String status = in.readLine();
            in.close();
            isr.close();
            if (status != null && !status.contains("\"status\": \"OK\"")) {
                return false;
            }
            Matcher ageMatcher = agePattern.matcher(status);
            if (!ageMatcher.find()) {
                return false;
            }
            long age = Long.parseLong(ageMatcher.group(1));
            return (age > Long.parseLong(props.getProperty("cacheStatusMaxAge")));
        } finally {
            in.close();
            isr.close();
        }
    }

    private void sendEmail(final String recipient)
            throws IOException, AddressException, MessagingException {
        Properties props = getProperties();

        String subject = props.getProperty("cacheStatusMaxAgeSubject");
        String htmlMessage = createHtmlEmailBody();

        this.getMailUtils().sendEmail(recipient, subject, htmlMessage, null, props);
    }

    private String createHtmlEmailBody() {
        String htmlMessage = "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@<br/>"
                + "@ WARNING: CACHE IS NOT REFRESHING!<br/>"
                + "@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@<br/>"
                + "IT IS POSSIBLE THAT SOMEONE IS DOING SOMETHING NASTY!<br/>"
                + "Someone could be eavesdropping on you right now (man-in-the-middle attack)!<br/>"
                + "It is also possible that an ACB just screwed something up somewhere "
                + "that we didn't catch. ~\\(-.-)/~</pre>";
        return htmlMessage;
    }
}
