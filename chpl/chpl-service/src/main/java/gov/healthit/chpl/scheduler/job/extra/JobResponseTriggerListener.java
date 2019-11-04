package gov.healthit.chpl.scheduler.job.extra;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.Trigger;
import org.quartz.Trigger.CompletedExecutionInstruction;
import org.quartz.TriggerListener;
import org.springframework.core.env.Environment;

public class JobResponseTriggerListener implements TriggerListener {
    private static final String TRIGGER_LISTENER_NAME = "UpdateSingleListingTriggerListener";
    private Logger logger;

    private List<JobResponseTriggerWrapper> triggerWrappers = new ArrayList<JobResponseTriggerWrapper>();
    private String email;
    private Environment env;

    public JobResponseTriggerListener(List<JobResponseTriggerWrapper> triggerWrappers, final String email,
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

        JobResponseTriggerWrapper wrapper = triggerWrappers.stream()
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

    private boolean areAllTriggersComplete() {
        // Have all triggers completed?
        long countOfIncomplete = triggerWrappers.stream()
                .filter(trig -> !trig.isCompleted())
                .count();

        return countOfIncomplete == 0L;
    }

    private void sendEmail() {
        (new JobResponseEmail()).sendEmail(env, email, triggerWrappers, logger);
    }

}
