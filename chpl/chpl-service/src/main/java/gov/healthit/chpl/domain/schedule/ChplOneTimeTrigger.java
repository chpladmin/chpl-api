package gov.healthit.chpl.domain.schedule;

import java.io.Serializable;

public class ChplOneTimeTrigger implements Serializable {
    private static final long serialVersionUID = -3583084568526672515L;

    private ChplJob job;
    private Long runDateMillis;

    public ChplJob getJob() {
        return job;
    }

    public void setJob(ChplJob job) {
        this.job = job;
    }

    public Long getRunDateMillis() {
        return runDateMillis;
    }

    public void setRunDateMillis(Long runDateMillis) {
        this.runDateMillis = runDateMillis;
    }

}
