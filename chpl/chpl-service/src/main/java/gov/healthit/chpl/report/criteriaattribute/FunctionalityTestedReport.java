package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FunctionalityTestedReport {
    private CertificationCriterion criterion;
    private FunctionalityTested functionalityTested;
    private Long count;
}
