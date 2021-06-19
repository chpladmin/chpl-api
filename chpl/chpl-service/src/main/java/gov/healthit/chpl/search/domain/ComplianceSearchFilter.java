package gov.healthit.chpl.search.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ComplianceSearchFilter implements Serializable {
    private static final long serialVersionUID = 1326187628639701580L;

    //set to true to find only listings that have had compliance activities at some point
    //set to false to find only listings that have never had a compliance activity
    //default is null - don't care about compliance
    @Builder.Default
    private Boolean hasHadComplianceActivity = null;

    @Builder.Default
    private Set<NonconformitySearchOptions> nonconformityOptions = new HashSet<NonconformitySearchOptions>();
    @Builder.Default
    private SearchSetOperator nonconformityOptionsOperator = SearchSetOperator.OR;
}
