package gov.healthit.chpl.web.controller.results;

import gov.healthit.chpl.domain.NonconformityTypeStatistics;

import java.util.ArrayList;
import java.util.List;

public class NonconformityTypeStatisticsResult {

	private List<NonconformityTypeStatistics> statisticsResult;

    /**
     * Default constructor.
     */
    public NonconformityTypeStatisticsResult() {
        this.statisticsResult = new ArrayList<NonconformityTypeStatistics>();
    }

    public List<NonconformityTypeStatistics> getStatisticsResult() {
        return statisticsResult;
    }

    public void setStatisticsResult(
            final List<NonconformityTypeStatistics> statisticsResult) {
        this.statisticsResult = statisticsResult;
    }
}
