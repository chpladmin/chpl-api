package gov.healthit.chpl.testtool;

import gov.healthit.chpl.domain.CertificationCriterion;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TestToolCriteriaMap {
    private Long id;
    private TestTool criteriaAttribute;
    private CertificationCriterion criterion;
}
