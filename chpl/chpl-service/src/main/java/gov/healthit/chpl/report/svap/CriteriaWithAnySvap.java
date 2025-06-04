package gov.healthit.chpl.report.svap;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaWithAnySvap {
    private CertificationCriterion certificationCriterion;
    private Long listingCountAttestingToCriteria;
    private Long listingCountAttestingToCriteriaAndAnySvap;
    private String certificationStatusName;
    private Integer sortOrder;
}
