package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SvapReport {
    private CertificationCriterion criterion;
    private Svap svap;
    private Long count;
}
