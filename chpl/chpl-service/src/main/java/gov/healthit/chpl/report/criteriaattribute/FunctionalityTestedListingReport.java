package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.functionalitytested.FunctionalityTested;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FunctionalityTestedListingReport {
    private String chplProductNumber;
    private CertificationCriterion criterion;
    private FunctionalityTested functionalityTested;
}
