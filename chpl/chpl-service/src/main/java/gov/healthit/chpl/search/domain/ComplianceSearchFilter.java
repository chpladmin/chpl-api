package gov.healthit.chpl.search.domain;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlTransient;

import com.fasterxml.jackson.annotation.JsonIgnore;

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

    @JsonIgnore
    @XmlTransient
    private Boolean hasHadComplianceActivityString;
    //set to true to find only listings that have had compliance activities at some point
    //set to false to find only listings that have never had a compliance activity
    //default is null - don't care about compliance
    private Boolean hasHadComplianceActivity;

    @Builder.Default
    private Set<NonconformitySearchOptions> nonconformityOptions = new HashSet<NonconformitySearchOptions>();
    @Builder.Default
    private SearchSetOperator nonconformityOptionsOperator = null;
}
