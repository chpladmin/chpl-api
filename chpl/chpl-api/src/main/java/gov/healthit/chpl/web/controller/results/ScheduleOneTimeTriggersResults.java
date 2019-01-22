package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;

public class ScheduleOneTimeTriggersResults implements Serializable {
    private static final long serialVersionUID = 3881680453356566850L;

    private List<ChplOneTimeTrigger> results;

    /**
     * Default constructor.
     */
    public ScheduleOneTimeTriggersResults() {
        results = new ArrayList<ChplOneTimeTrigger>();
    }

    public List<ChplOneTimeTrigger> getResults() {
        return results;
    }

    public void setResults(final List<ChplOneTimeTrigger> results) {
        this.results = results;
    }

}
