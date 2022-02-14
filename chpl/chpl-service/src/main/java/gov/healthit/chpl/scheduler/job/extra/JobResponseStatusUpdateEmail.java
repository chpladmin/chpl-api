package gov.healthit.chpl.scheduler.job.extra;

import java.util.ArrayList;
import java.util.Arrays;

import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.email.ChplEmailFactory;

public class JobResponseStatusUpdateEmail {

    public void sendEmail(Environment env, String emailAddress, String jobName,
            Long totalJobs, Long completedJobs, Logger logger, ChplEmailFactory chplEmailFactory) {
        try {
            chplEmailFactory.emailBuilder()
                    .recipients(new ArrayList<String>(Arrays.asList(emailAddress)))
                    .subject(jobName + " -- completed " + completedJobs + " out of " + totalJobs + " jobs")
                    .sendEmail();
        } catch (Exception e) {
            logger.error("Error sending email: " + e.getMessage(), e);
        }
    }

}
