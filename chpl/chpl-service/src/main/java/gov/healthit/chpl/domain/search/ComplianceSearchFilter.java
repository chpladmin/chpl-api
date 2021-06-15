package gov.healthit.chpl.domain.search;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.Data;

@Data
public class ComplianceSearchFilter implements Serializable {
    private static final long serialVersionUID = 1326187628639701580L;

    //set to true to find only listings that have had compliance activities at some point
    //set to false to find only listings that have never had a compliance activity
    //default is null - don't care about compliance
    private Boolean hasHadComplianceActivity = null;

    private Set<NonconformitySearchOptions> nonconformityOptions = new HashSet<NonconformitySearchOptions>();
    private SearchSetOperator nonconformityOptionsOperator = SearchSetOperator.OR;
}
