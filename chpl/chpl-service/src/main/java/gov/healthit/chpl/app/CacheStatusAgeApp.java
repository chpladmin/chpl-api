package gov.healthit.chpl.app;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.permission.GrantedPermission;
import gov.healthit.chpl.domain.concept.NotificationTypeConcept;
import gov.healthit.chpl.dto.notification.RecipientWithSubscriptionsDTO;

/**
 * Application to check the listing cache age and notify if it's "too old".
 * @author alarned
 *
 */
@Component("cacheStatusAgeApp")
public class CacheStatusAgeApp extends NotificationEmailerReportApp {

    /**
     * Default constructor.
     */
    public CacheStatusAgeApp() {
    }

    /**
     * Main method. Checks to see if the cache is old, then, if it is,
     * sends email messages to subscribers of that notification.
     * @param args none expected
     * @throws Exception if necessary
     */
    public static void main(final String[] args) throws Exception {
        CacheStatusAgeApp app = new CacheStatusAgeApp();
        app.setLocalContext();
        AbstractApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        app.initiateSpringBeans(context);

        // Check cache status
        if (app.isCacheOld()) {
            // Get recipients
            Set<GrantedPermission> permissions = new HashSet<GrantedPermission>();
            permissions.add(new GrantedPermission("ROLE_ADMIN"));
            List<RecipientWithSubscriptionsDTO> recipientSubscriptions = app.getNotificationDAO()
                    .getAllNotificationMappingsForType(permissions,
                            NotificationTypeConcept.CACHE_STATUS_AGE_NOTIFICATION,
                            null);
            if (recipientSubscriptions.size() > 0) {
                // send emails
                app.sendEmail(recipientSubscriptions);
            }
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
            long duration = (new Date()).getTime() - age;
            return (duration > Long.parseLong(props.getProperty("cacheStatusMaxAge")));
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
