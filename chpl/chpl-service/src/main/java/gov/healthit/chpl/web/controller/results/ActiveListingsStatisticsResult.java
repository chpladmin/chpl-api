package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.ActiveListingsStatistics;

/**
 * Represents a list of ActiveListingsStatistics domain objects.
 * @author alarned
 *
 */
public class ActiveListingsStatisticsResult {
    private List<ActiveListingsStatistics> activeListingsStatisticsResult;

    /**
     * Default constructor.
     */
    public ActiveListingsStatisticsResult() {
        this.activeListingsStatisticsResult = new ArrayList<ActiveListingsStatistics>();
    }

    public List<ActiveListingsStatistics> getActiveListingsStatisticsResult() {
        return activeListingsStatisticsResult;
    }

    public void setActiveListingsStatisticsResult(
            final List<ActiveListingsStatistics> activeListingsStatisticsResult) {
        this.activeListingsStatisticsResult = activeListingsStatisticsResult;
    }

}
