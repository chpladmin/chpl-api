package gov.healthit.chpl.criteriaattribute.functionalitytested;

import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FunctionalityTestedCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private FunctionalityTested functionalityTested;
}
