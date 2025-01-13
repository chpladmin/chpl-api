package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OptionalStandardReport {
    private CertificationCriterion criterion;
    private OptionalStandard optionalStandard;
    private Long count;
}
