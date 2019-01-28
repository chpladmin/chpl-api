package gov.healthit.chpl.web.controller.results;

import gov.healthit.chpl.domain.NonconformityTypeStatistics;

import java.util.ArrayList;
import java.util.List;

public class NonconformityTypeStatisticsResult {

    private List<NonconformityTypeStatistics> nonconformityStatisticsResult;

    /**
     * Default constructor.
     */
    public NonconformityTypeStatisticsResult() {
        this.nonconformityStatisticsResult = new ArrayList<NonconformityTypeStatistics>();
    }

    public List<NonconformityTypeStatistics> getNonconformityStatisticsResult() {
        return nonconformityStatisticsResult;
    }

    public void setNonconformityStatisticsResult(
            final List<NonconformityTypeStatistics> nonconformityStatisticsResult) {
        this.nonconformityStatisticsResult = nonconformityStatisticsResult;
    }
}
