package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.schedule.ChplRepeatableTrigger;

/**
 * Results object for Schedule Triggers.
 * @author alarned
 *
 */
public class ScheduleTriggersResults implements Serializable {
    private static final long serialVersionUID = 8748244450112564530L;
    private List<ChplRepeatableTrigger> results;

    /**
     * Default constructor.
     */
    public ScheduleTriggersResults() {
        results = new ArrayList<ChplRepeatableTrigger>();
    }

    public List<ChplRepeatableTrigger> getResults() {
        return results;
    }

    public void setResults(final List<ChplRepeatableTrigger> results) {
        this.results = results;
    }
}
