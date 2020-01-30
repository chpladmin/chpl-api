package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.schedule.ScheduledSystemJob;

/**
 * Results object for System Triggers.
 * @author dbrown
 *
 */
public class SystemTriggerResults implements Serializable {
    private static final long serialVersionUID = 3340877999946652675L;
    private List<ScheduledSystemJob> results;

    /**
     * Default constructor.
     */
    public SystemTriggerResults() {
        results = new ArrayList<ScheduledSystemJob>();
    }

    public SystemTriggerResults(List<ScheduledSystemJob> results) {
        this.results = new ArrayList<ScheduledSystemJob>(results);
    }

    public List<ScheduledSystemJob> getResults() {
        return results;
    }

    public void setResults(final List<ScheduledSystemJob> results) {
        this.results = results;
    }
}
