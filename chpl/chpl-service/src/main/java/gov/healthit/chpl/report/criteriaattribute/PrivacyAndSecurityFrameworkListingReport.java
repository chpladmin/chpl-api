package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PrivacyAndSecurityFrameworkListingReport {
    private String chplProductNumber;
    private String listingDetailsUrl;
    private CertificationCriterion criterion;
    private String privacyAndSecurityFramework;
}
