package gov.healthit.chpl.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
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

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

/**
 * Job run by Scheduler to send emails when the cache is "too old".
 * @author alarned
 *
 */
@Component("cacheStatusAgeApp")
public class CacheStatusAgeApp extends NotificationEmailerReportApp implements Job {
    private AbstractApplicationContext context;
    /**
     * Default constructor.
     * @throws Exception if context can't be configured
     */
    public CacheStatusAgeApp() throws Exception {
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
                // Get recipients
                Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
                permissions.add(new GrantedPermission("ROLE_ADMIN"));
                List<RecipientWithSubscriptionsDTO> recipientSubscriptions = getNotificationDAO()
                        .getAllNotificationMappingsForType(permissions,
                                NotificationTypeConcept.CACHE_STATUS_AGE_NOTIFICATION,
                                null);
                if (recipientSubscriptions.size() > 0) {
                    // send emails
                    try {
                        sendEmail(recipientSubscriptions);
                    } catch (IOException | MessagingException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        context.close();
    }

    private boolean isCacheOld() throws IOException {
        Properties props = getProperties();
        Pattern agePattern = Pattern.compile("\"age\": (\\d*)");

        try {
            URL statusUrl = new URL(props.getProperty("cacheStatusMaxAgeUrl"));
            BufferedReader in = new BufferedReader(new InputStreamReader(statusUrl.openStream()));
            String status = in.readLine();
            if (!status.contains("\"status\": \"OK\"")) {
                return false;
            }
            in.close();
            Matcher ageMatcher = agePattern.matcher(status);
            if (!ageMatcher.find()) {
                return false;
            }
            long age = Long.parseLong(ageMatcher.group(1));
            return (age > Long.parseLong(props.getProperty("cacheStatusMaxAge")));
        } catch (FileNotFoundException e) {
            LOGGER.info("Error getting status; message: " + e.getMessage());
        } catch (IOException e) {
            LOGGER.info("Error getting status; message: " + e.getMessage());
        }
        return false;
    }

    private void sendEmail(final List<RecipientWithSubscriptionsDTO> recipientSubscriptions)
            throws IOException, AddressException, MessagingException {
        Properties props = getProperties();

        String subject = props.getProperty("cacheStatusMaxAgeSubject");
        String htmlMessage;

        // get email addresses
        Set<String> emails = new HashSet<String>();
        for (RecipientWithSubscriptionsDTO recip : recipientSubscriptions) {
            emails.add(recip.getEmail());
        }
        String[] bccEmail = emails.toArray(new String[emails.size()]);

        if (bccEmail.length > 0) {
            htmlMessage = createHtmlEmailBody();
            this.getMailUtils().sendEmail(null, bccEmail, subject, htmlMessage, null, props);
        }
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

    class ExtendedCacheStatus {
        private String status;
        private long age;

        ExtendedCacheStatus() { }

        public String getStatus() {
            return status;
        }

        public void setStatus(final String status) {
            this.status = status;
        }

        public long getAge() {
            return age;
        }

        public void setAge(final long age) {
            this.age = age;
        }
    }
}
