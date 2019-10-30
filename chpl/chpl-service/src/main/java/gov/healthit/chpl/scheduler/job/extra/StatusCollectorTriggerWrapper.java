package gov.healthit.chpl.scheduler.job.extra;

import org.quartz.Trigger;

public class StatusCollectorTriggerWrapper {

    private Trigger trigger;
    private boolean completed = false;
    private JobResponse jobResponse;

    public StatusCollectorTriggerWrapper(final Trigger trigger) {
        this.trigger = trigger;
    }

    public Trigger getTrigger() {
        return trigger;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public JobResponse getJobResponse() {
        return jobResponse;
    }

    public void setJobResponse(JobResponse jobResponse) {
        this.jobResponse = jobResponse;
    }

}
