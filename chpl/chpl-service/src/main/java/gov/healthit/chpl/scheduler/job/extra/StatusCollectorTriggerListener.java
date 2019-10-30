package gov.healthit.chpl.scheduler.job.extra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.util.EmailBuilder;

public class StatusCollectorTriggerListener implements TriggerListener {
    private static final String TRIGGER_LISTENER_NAME = "UpdateSingleListingTriggerListener";
    private Logger logger;

    private List<StatusCollectorTriggerWrapper> triggerWrappers = new ArrayList<StatusCollectorTriggerWrapper>();
    private String email;
    private Environment env;

    public StatusCollectorTriggerListener(List<StatusCollectorTriggerWrapper> triggerWrappers, final String email,
            final Environment env, final Logger logger) {
        this.triggerWrappers = triggerWrappers;
        this.email = email;
        this.env = env;
        this.logger = logger;
    }

    @Override
    public String getName() {
        return TRIGGER_LISTENER_NAME;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        // Do nothing
    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        // Do nothing
    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {

        StatusCollectorTriggerWrapper wrapper = triggerWrappers.stream()
                .filter(item -> item.getTrigger().getKey().equals(trigger.getKey()))
                .findFirst()
                .get();

        wrapper.setCompleted(true);
        wrapper.setJobResponse((JobResponse) context.getResult());

        if (areAllTriggersComplete()) {
            logger.info("All jobs have completed!!!!!");
            sendEmail();
        }
    }

    private String getStatusAsString(final boolean success) {
        return success ? "Success " : "Failure";
    }

    private boolean areAllTriggersComplete() {
        // Have all triggers completed?
        long countOfIncomplete = triggerWrappers.stream()
                .filter(trig -> !trig.isCompleted())
                .count();

        return countOfIncomplete == 0L;
    }

    private void sendEmail() {
        try {
            new EmailBuilder(env)
                    .recipients(new ArrayList<String>(Arrays.asList(email)))
                    .subject("Retire 2014 Listing Job Report")
                    .htmlMessage(buildTable())
                    .sendEmail();
        } catch (MessagingException e) {
            logger.error("Error sending email: " + e.getMessage(), e);
        }
    }

    private String buildTable() {
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");

        triggerWrappers.stream()
                .forEach(wrapper -> sb.append(buildRow(wrapper)));

        sb.append("<table>");
        return sb.toString();
    }

    private String buildRow(StatusCollectorTriggerWrapper wrapper) {
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
}
