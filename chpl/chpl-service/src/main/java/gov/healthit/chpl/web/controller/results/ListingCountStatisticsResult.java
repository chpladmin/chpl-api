package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.ListingCountStatistics;

/**
 * Represents a list of ListingCountStatistics domain objects.
 * @author alarned
 *
 */
public class ListingCountStatisticsResult {
    private List<ListingCountStatistics> statisticsResult;

    /**
     * Default constructor.
     */
    public ListingCountStatisticsResult() {
        this.statisticsResult = new ArrayList<ListingCountStatistics>();
    }

    public List<ListingCountStatistics> getStatisticsResult() {
        return statisticsResult;
    }

    public void setStatisticsResult(
            final List<ListingCountStatistics> statisticsResult) {
        this.statisticsResult = statisticsResult;
    }

}
