package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.CriterionProductStatistics;

/**
 * Represents a list of CriterionProductStatistics domain objects.
 * @author alarned
 *
 */
public class CriterionProductStatisticsResult {
    private List<CriterionProductStatistics> criterionProductStatisticsResult;

    /**
     * Default constructor.
     */
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
