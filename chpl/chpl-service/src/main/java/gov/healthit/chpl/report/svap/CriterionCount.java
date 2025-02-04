package gov.healthit.chpl.report.svap;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CriterionCount {
    private CertificationCriterion criterion;
    private Long count;
}
