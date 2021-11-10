package gov.healthit.chpl.scheduler.job.extra;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.email.EmailBuilder;

public class JobResponseStatusUpdateEmail {

    public void sendEmail(final Environment env, final String emailAddress, final String jobName,
            final Long totalJobs, final Long completedJobs, final Logger logger) {
        try {
            new EmailBuilder(env)
                    .recipients(new ArrayList<String>(Arrays.asList(emailAddress)))
                    .subject(jobName + " -- completed " + completedJobs + " out of " + totalJobs + " jobs")
                    .sendEmail();
        } catch (Exception e) {
            logger.error("Error sending email: " + e.getMessage(), e);
        }
    }

}
