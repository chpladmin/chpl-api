package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.TestData;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestDataReport {
    private CertificationCriterion criterion;
    private TestData testData;
    private Long count;
}
