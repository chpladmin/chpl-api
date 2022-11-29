package gov.healthit.chpl.scheduler.job;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.email.ChplEmailFactory;
import gov.healthit.chpl.exception.EmailNotSentException;

public class CacheStatusAgeJob implements Job {
    private static final Logger LOGGER = LogManager.getLogger("cacheStatusAgeJobLogger");

    @Autowired
    private Environment env;

    @Autowired
    private ChplEmailFactory chplEmailFactory;

    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Cache Status Age job. *********");
        try {
            if (isCacheOld()) {
                String recipient = jobContext.getMergedJobDataMap().getString("email");
                try {
                    sendEmail(recipient);
                } catch (IOException | EmailNotSentException e) {
                    LOGGER.error(e);
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Cache Status Age job. *********");
    }

    private boolean isCacheOld() throws UnsupportedEncodingException, IOException {
        Pattern agePattern = Pattern.compile("\"age\": (\\d*)");
        URL statusUrl = new URL(env.getProperty("cacheStatusMaxAgeUrl"));
        InputStreamReader isr = new InputStreamReader(statusUrl.openStream(), "UTF-8");
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
            long age;
            try {
                age = Long.parseLong(ageMatcher.group(1));
            } catch (NumberFormatException nfe) {
                LOGGER.error("Unable to parse cache status age", nfe);
                age = -1;
            }
            return (age > Long.parseLong(env.getProperty("cacheStatusMaxAge")));
        } finally {
            in.close();
            isr.close();
        }
    }

    private void sendEmail(String recipient) throws IOException, EmailNotSentException {
        LOGGER.info("Sending email to: " + recipient);
        String subject = env.getProperty("cacheStatusMaxAgeSubject");
        String htmlMessage = createHtmlEmailBody();
        LOGGER.info("Message to be sent: " + htmlMessage);

        List<String> addresses = new ArrayList<String>();
        addresses.add(recipient);

        chplEmailFactory.emailBuilder().recipients(addresses)
                .subject(subject)
                .htmlMessage(htmlMessage)
                .sendEmail();
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
