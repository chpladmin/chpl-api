package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class SurveillanceSearchFilter implements Serializable {
    private static final long serialVersionUID = 1329207628639701580L;

    //search for one or more states of nonconformities
    Set<NonconformitySearchOptions> nonconformityOptions =
            new HashSet<NonconformitySearchOptions>();

    SearchSetOperator nonconformityOptionsOperator = SearchSetOperator.OR;

    //set to true to find only listings that have had surveillance at some point
    //set to false to find only listings that have never had surveillance
    //default is null - don't care about surveillance
    Boolean hasHadSurveillance = null;

    public Set<NonconformitySearchOptions> getNonconformityOptions() {
        return nonconformityOptions;
    }

    public void setNonconformityOptions(final Set<NonconformitySearchOptions> nonconformityOptions) {
        this.nonconformityOptions = nonconformityOptions;
    }

    public Boolean getHasHadSurveillance() {
        return hasHadSurveillance;
    }

    public void setHasHadSurveillance(final Boolean hasHadSurveillance) {
        this.hasHadSurveillance = hasHadSurveillance;
    }

    public SearchSetOperator getNonconformityOptionsOperator() {
        return nonconformityOptionsOperator;
    }

    public void setNonconformityOptionsOperator(SearchSetOperator nonconformityOptionsOperator) {
        this.nonconformityOptionsOperator = nonconformityOptionsOperator;
    }
}
