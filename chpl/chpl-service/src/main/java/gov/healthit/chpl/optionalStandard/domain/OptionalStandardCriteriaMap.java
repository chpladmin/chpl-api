package gov.healthit.chpl.optionalStandard.domain;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OptionalStandardCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private OptionalStandard optionalStandard;
}
