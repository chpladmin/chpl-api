package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codeset.CodeSet;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CodeSetListingReport {
    private String chplProductNumber;
    private CertificationCriterion criterion;
    private CodeSet codeSet;
}
