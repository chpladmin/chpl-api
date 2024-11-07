package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.standard.Standard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StandardListingReport {
    private String chplProductNumber;
    private CertificationCriterion criterion;
    private Standard standard;
}
