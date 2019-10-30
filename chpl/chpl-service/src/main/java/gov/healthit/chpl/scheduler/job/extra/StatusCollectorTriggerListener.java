package gov.healthit.chpl.scheduler.job.extra;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.mail.MessagingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.springframework.core.env.Environment;

import gov.healthit.chpl.util.EmailBuilder;

public class StatusCollectorTriggerListener implements TriggerListener {

    private static final String TRIGGER_LISTENER_NAME = "UpdateSingleListingTriggerListener";
    private static final Logger LOGGER = LogManager.getLogger("updateListingStatusJobLogger");

    private List<StatusCollectorTriggerWrapper> triggerWrappers = new ArrayList<StatusCollectorTriggerWrapper>();
    private String email;
    private Environment env;

    public StatusCollectorTriggerListener(final String email, final Environment env) {
        this.email = email;
        this.env = env;
    }

    @Override
    public String getName() {
        return TRIGGER_LISTENER_NAME;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {

    }

    @Override
    public boolean vetoJobExecution(Trigger trigger, JobExecutionContext context) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void triggerMisfired(Trigger trigger) {
        // TODO Auto-generated method stub

    }

    @Override
    public void triggerComplete(Trigger trigger, JobExecutionContext context,
            CompletedExecutionInstruction triggerInstructionCode) {
        // LOGGER.info(getName() + " trigger: " + trigger.getKey() + " completed at " + trigger.getStartTime());

        StatusCollectorTriggerWrapper wrapper = triggerWrappers.stream()
                .filter(item -> item.getTrigger().getKey().equals(trigger.getKey()))
                .findFirst()
                .get();

        wrapper.setCompleted(true);
        wrapper.setJobResponse((JobResponse) context.getResult());

        if (areAllTriggersComplete()) {
            LOGGER.info("All jobs have completed!!!!!");
            // triggerWrappers.stream()
            // .forEach(item -> LOGGER
            // .info(item.getTrigger().getJobDataMap().getLong("listing") + " - completed ["
            // + getStatusAsString(item.isCompletedSuccessfully()) + "] - "
            // + item.getMessage()));
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
            // TODO Auto-generated catch block
            e.printStackTrace();
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

    public List<StatusCollectorTriggerWrapper> getTriggerWrappers() {
        return triggerWrappers;
    }

    public void setTriggerWrappers(List<StatusCollectorTriggerWrapper> triggerWrappers) {
        this.triggerWrappers = triggerWrappers;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

}
