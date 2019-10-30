package gov.healthit.chpl.scheduler.job.extra;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;

public class StatusCollectorTriggerListener implements TriggerListener {

    private static final String TRIGGER_LISTENER_NAME = "UpdateSingleListingTriggerListener";
    private static final Logger LOGGER = LogManager.getLogger("updateListingStatusJobLogger");

    private List<StatusCollectorTriggerWrapper> triggerWrappers = new ArrayList<StatusCollectorTriggerWrapper>();

    public StatusCollectorTriggerListener() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public String getName() {
        return TRIGGER_LISTENER_NAME;
    }

    @Override
    public void triggerFired(Trigger trigger, JobExecutionContext context) {
        String triggerName = context.getJobDetail().getKey().toString();
        LOGGER.info("trigger : " + triggerName + " is fired");

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
        LOGGER.info(getName() + " trigger: " + trigger.getKey() + " completed at " + trigger.getStartTime());

        StatusCollectorTriggerWrapper wrapper = triggerWrappers.stream()
                .filter(item -> item.getTrigger().getKey().equals(trigger.getKey()))
                .findFirst()
                .get();

        wrapper.setCompleted(true);
        wrapper.setCompletedSuccessfully(trigger.getJobDataMap().getBoolean("success"));
        wrapper.setMessage(trigger.getJobDataMap().getString("message"));

        if (areAllTriggersComplete()) {
            LOGGER.info("All jobs have completed!!!!!");
            triggerWrappers.stream()
                    .forEach(item -> LOGGER
                            .info(item.getTrigger().getJobDataMap().getLong("listing") + " - completed ["
                                    + getStatusAsString(item.getTrigger().getJobDataMap().getBoolean("success"))
                                    + "] - "
                                    + item.getTrigger().getJobDataMap().getString("message")));
        } else {
            LOGGER.info("There are jobs are still running");
        }

    }

    private String getStatusAsString(final boolean success) {
        return success ? "Successfully " : "Unsuccessfully";
    }

    private boolean areAllTriggersComplete() {
        // Have all triggers completed?
        long countOfIncomplete = triggerWrappers.stream()
                .filter(trig -> !trig.isCompleted())
                .count();

        return countOfIncomplete == 0L;
    }

    public List<StatusCollectorTriggerWrapper> getTriggerWrappers() {
        return triggerWrappers;
    }

    public void setTriggerWrappers(List<StatusCollectorTriggerWrapper> triggerWrappers) {
        this.triggerWrappers = triggerWrappers;
    }

}
