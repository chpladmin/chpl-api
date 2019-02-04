package gov.healthit.chpl.domain.schedule;

public abstract class ChplTrigger {
    private ChplJob job;

    public ChplJob getJob() {
        return job;
    }

    public void setJob(ChplJob job) {
        this.job = job;
    }
}
