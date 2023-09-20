package gov.healthit.chpl.conformanceMethod.domain;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConformanceMethodCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private ConformanceMethod conformanceMethod;
}
