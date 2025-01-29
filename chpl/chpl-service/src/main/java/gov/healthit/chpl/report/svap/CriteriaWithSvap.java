package gov.healthit.chpl.report.svap;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CriteriaWithSvap {
    private CertificationCriterion certificationCriterion;
    private Svap svap;
    private Long activeListingCountAttestingToSvap;
    private Integer sortOrder;
}
