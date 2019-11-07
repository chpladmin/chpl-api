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
    private String emailAddress;
    private String emailFileName;
    private String emailSubject;
    private Integer statusInterval;
    private Environment env;

    public JobResponseTriggerListener(List<JobResponseTriggerWrapper> triggerWrappers, final String emailAddress,
            final String emailFilename, final String emailSubject, final Integer statusInterval, final Environment env,
            final Logger logger) {
        this.triggerWrappers = triggerWrappers;
        this.emailAddress = emailAddress;
        this.emailFileName = emailFilename;
        this.emailSubject = emailSubject;
        this.statusInterval = statusInterval;
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

        sendIntermittantStatusUpdate(trigger);

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

    private void sendIntermittantStatusUpdate(final Trigger trigger) {
        Long completedJobsCount = getCompletedJobCount();

        if (completedJobsCount % statusInterval == 0) {
            (new JobResponseStatusUpdateEmail()).sendEmail(
                    env,
                    emailAddress,
                    trigger.getJobKey().getName(),
                    Long.valueOf(triggerWrappers.size()),
                    triggerWrappers.stream().filter(trig -> trig.isCompleted()).count(),
                    logger);

            logger.info("----------------------------Completed " + completedJobsCount + " out of "
                    + triggerWrappers.size() + " jobs----------------------------");
        }
    }

    private Long getCompletedJobCount() {
        return triggerWrappers.stream().filter(trig -> trig.isCompleted()).count();
    }

    private void sendEmail() {
        (new JobResponseEmail()).sendEmail(env, emailAddress, emailFileName, emailSubject, triggerWrappers, logger);
    }

}
