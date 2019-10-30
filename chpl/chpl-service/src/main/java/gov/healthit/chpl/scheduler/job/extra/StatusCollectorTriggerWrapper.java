package gov.healthit.chpl.scheduler.job.extra;

import org.quartz.Trigger;

public class StatusCollectorTriggerWrapper {

    private Trigger trigger;
    private boolean completed = false;
    private boolean completedSuccessfully = false;
    private String message = "";

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

    public boolean isCompletedSuccessfully() {
        return completedSuccessfully;
    }

    public void setCompletedSuccessfully(boolean completedSuccessfully) {
        this.completedSuccessfully = completedSuccessfully;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
