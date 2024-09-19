package gov.healthit.chpl.report.criteriaattribute;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.testtool.TestTool;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestToolReport {
    private CertificationCriterion criterion;
    private TestTool testTool;
    private Long count;
}
