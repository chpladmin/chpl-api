package gov.healthit.chpl.domain.schedule;

import java.io.Serializable;

public class ChplOneTimeTrigger extends ChplTrigger implements Serializable {
    private static final long serialVersionUID = -3583084568526672515L;

    private Long runDateMillis;

    public Long getRunDateMillis() {
        return runDateMillis;
    }

    public void setRunDateMillis(Long runDateMillis) {
        this.runDateMillis = runDateMillis;
    }
}
