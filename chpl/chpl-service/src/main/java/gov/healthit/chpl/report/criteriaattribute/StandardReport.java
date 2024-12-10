package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.standard.Standard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandardReport {
    private CertificationCriterion criterion;
    private Standard standard;
    private Long count;
}
