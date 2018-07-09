package gov.healthit.chpl.web.controller.results;

import java.util.ArrayList;
import java.util.List;

import gov.healthit.chpl.domain.IncumbentDevelopersStatistics;

/**
 * Represents a list of CriterionProductStatistics domain objects.
 * @author alarned
 *
 */
public class IncumbentDevelopersStatisticsResult {
    private List<IncumbentDevelopersStatistics> incumbentDevelopersStatisticsResult;

    /**
     * Default constructor.
     */
    public IncumbentDevelopersStatisticsResult() {
        this.incumbentDevelopersStatisticsResult = new ArrayList<IncumbentDevelopersStatistics>();
    }

    public List<IncumbentDevelopersStatistics> getIncumbentDevelopersStatisticsResult() {
        return incumbentDevelopersStatisticsResult;
    }

    public void setIncumbentDevelopersStatisticsResult(
            final List<IncumbentDevelopersStatistics> incumbentDevelopersStatisticsResult) {
        this.incumbentDevelopersStatisticsResult = incumbentDevelopersStatisticsResult;
    }

}
