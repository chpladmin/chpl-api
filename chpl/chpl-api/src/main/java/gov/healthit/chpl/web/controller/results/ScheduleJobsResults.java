package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.schedule.ChplJob;

public class ScheduleJobsResults implements Serializable{
    private static final long serialVersionUID = -2991204404213846766L;

    private List<ChplJob> results;
    
    /**
     * Default constructor.
     */
    public ScheduleJobsResults() {
        results = new ArrayList<ChplJob>();
    }

    public List<ChplJob> getResults() {
        return results;
    }

    public void setResults(final List<ChplJob> results) {
        this.results = results;
    }
}
