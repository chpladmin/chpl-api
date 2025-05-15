package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.svap.domain.Svap;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SvapReportByCertificationStatus {
    private CertificationCriterion criterion;
    private Svap svap;
    private String certificationStatus;
    private Long count;
}
