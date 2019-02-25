package gov.healthit.chpl.domain.schedule;

/**
 * Basic starting point for CHPL Triggers.
 * @author alarned
 *
 */
public abstract class ChplTrigger {

    private ChplJob job;

    public ChplJob getJob() {
        return job;
    }

    public void setJob(final ChplJob job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return "ChplTrigger [job=" + job + "]";
    }
}
