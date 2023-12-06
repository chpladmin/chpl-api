package gov.healthit.chpl.standard;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StandardCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private Standard standard;
}
