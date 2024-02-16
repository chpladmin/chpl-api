package gov.healthit.chpl.codesetdate;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CodeSetCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private CodeSet codeSet;
}
