package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConformanceMethodListingReport {
    private String chplProductNumber;
    private String listingDetailsUrl;
    private CertificationCriterion criterion;
    private ConformanceMethod conformanceMethod;
}
