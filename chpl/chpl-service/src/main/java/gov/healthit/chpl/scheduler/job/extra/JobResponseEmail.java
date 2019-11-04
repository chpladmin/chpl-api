package gov.healthit.chpl.scheduler.job.extra;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.util.EmailBuilder;

public class JobResponseEmail {

    public void sendEmail(final Environment env, final String emailAddress, final String emailFilename,
            final String emailSubject,
            final List<JobResponseTriggerWrapper> triggerWrappers, final Logger logger) {
        try {
            new EmailBuilder(env)
                    .recipients(new ArrayList<String>(Arrays.asList(emailAddress)))
                    .subject(emailSubject)
                    .htmlMessage(buildTable(triggerWrappers))
                    .fileAttachments(getFileAttachments(triggerWrappers, emailFilename))
                    .sendEmail();
        } catch (Exception e) {
            logger.error("Error sending email: " + e.getMessage(), e);
        }
    }

    private String buildTable(final List<JobResponseTriggerWrapper> triggerWrappers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        sb.append(buildHeaderRow());
        sb.append(buildTableBody(triggerWrappers));
        sb.append("<table>");
        return sb.toString();
    }

    private String buildTableBody(final List<JobResponseTriggerWrapper> triggerWrappers) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tbody>");
        triggerWrappers.stream()
                .forEach(wrapper -> sb.append(buildRow(wrapper)));
        sb.append("</tbody>");
        return sb.toString();
    }

    private String buildHeaderRow() {
        StringBuilder sb = new StringBuilder();
        sb.append("<thead>");
        sb.append("<tr>");
        sb.append("<th>");
        sb.append("Identifier");
        sb.append("</th>");
        sb.append("<th>");
        sb.append("Success");
        sb.append("</th>");
        sb.append("<th>");
        sb.append("Message");
        sb.append("</th>");
        sb.append("</tr>");
        sb.append("</thead>");
        return sb.toString();
    }

    private String buildRow(JobResponseTriggerWrapper wrapper) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr>");
        sb.append("<td>");
        sb.append(wrapper.getJobResponse().getIdentifier());
        sb.append("</td>");
        sb.append("<td>");
        sb.append(getStatusAsString(wrapper.getJobResponse().isCompletedSuccessfully()));
        sb.append("</td>");
        sb.append("<td>");
        sb.append(wrapper.getJobResponse().getMessage().replace("\n", "<br />"));
        sb.append("</td>");
        sb.append("</tr>");
        return sb.toString();
    }

    private String getStatusAsString(final boolean success) {
        return success ? "Success " : "Failure";
    }

    private List<File> getFileAttachments(final List<JobResponseTriggerWrapper> triggerWrappers, final String fileName)
            throws IOException {
        List<JobResponse> responses = triggerWrappers.stream()
                .map(wrapper -> wrapper.getJobResponse())
                .collect(Collectors.toList());

        return new ArrayList<File>(Arrays.asList((new JobResponseCsvGenerator()).getCsvFile(responses, fileName)));
    }
}
