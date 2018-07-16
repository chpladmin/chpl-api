package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.schedule.ChplTrigger;

/**
 * Results object for Schedule Triggers.
 * @author alarned
 *
 */
public class ScheduleTriggersResults implements Serializable {
    private static final long serialVersionUID = 8748244450112564530L;
    private List<ChplTrigger> results;

    /**
     * Default constructor.
     */
    public ScheduleTriggersResults() {
        results = new ArrayList<ChplTrigger>();
    }

    public List<ChplTrigger> getResults() {
        return results;
    }

    public void setResults(final List<ChplTrigger> results) {
        this.results = results;
    }
}
