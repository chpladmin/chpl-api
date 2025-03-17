package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CriterionProductStatistics;

public class CriterionProductStatisticsResult {
    private List<CriterionProductStatistics> criterionProductStatisticsResult;

    public CriterionProductStatisticsResult() {
        this.criterionProductStatisticsResult = new ArrayList<CriterionProductStatistics>();
    }

    public List<CriterionProductStatistics> getCriterionProductStatisticsResult() {
        return criterionProductStatisticsResult;
    }

    public void setCriterionProductStatisticsResult(
            final List<CriterionProductStatistics> criterionProductStatisticsResult) {
        this.criterionProductStatisticsResult = criterionProductStatisticsResult;
    }

}