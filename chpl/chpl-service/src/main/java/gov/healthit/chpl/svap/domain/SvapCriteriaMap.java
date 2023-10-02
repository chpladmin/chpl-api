package gov.healthit.chpl.svap.domain;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SvapCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private Svap svap;
}
