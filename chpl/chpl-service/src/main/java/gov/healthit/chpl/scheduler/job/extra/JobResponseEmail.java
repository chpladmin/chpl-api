package gov.healthit.chpl.scheduler.job.extra;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.email.ChplEmailFactory;

public class JobResponseEmail {

    public void sendEmail(Environment env, String emailAddress, String emailFilename,
            String emailSubject, List<JobResponseTriggerWrapper> triggerWrappers, Logger logger,
            ChplEmailFactory chplEmailFactory) {
        try {
            chplEmailFactory.emailBuilder()
                    .recipients(new ArrayList<String>(Arrays.asList(emailAddress)))
                    .subject(emailSubject)
                    .htmlMessage(buildEmail(triggerWrappers))
                    .fileAttachments(getFileAttachments(triggerWrappers, emailFilename))
                    .sendEmail();
        } catch (Exception e) {
            logger.error("Error sending email: " + e.getMessage(), e);
        }
    }

    private String buildEmail(final List<JobResponseTriggerWrapper> triggerWrappers) {
        Long total = Long.valueOf(triggerWrappers.size());
        Long success = triggerWrappers.stream()
                .filter(wrapper -> wrapper.getJobResponse().isCompletedSuccessfully())
                .count();
        Long failed = triggerWrappers.stream()
                .filter(wrapper -> !wrapper.getJobResponse().isCompletedSuccessfully())
                .count();

        StringBuilder sb = new StringBuilder();
        sb.append("Total number of listings to update: ");
        sb.append(total);
        sb.append("<br />");
        sb.append("Total number of listings successfully updated: ");
        sb.append(success);
        sb.append("<br />");
        sb.append("Total number of listings that failed to update: ");
        sb.append(failed);
        sb.append("<br />");
        return sb.toString();
    }

    private List<File> getFileAttachments(final List<JobResponseTriggerWrapper> triggerWrappers, final String fileName)
            throws IOException {
        List<JobResponse> responses = triggerWrappers.stream()
                .map(wrapper -> wrapper.getJobResponse())
                .collect(Collectors.toList());

        return new ArrayList<File>(Arrays.asList((new JobResponseCsvGenerator()).getCsvFile(responses, fileName)));
    }
}
