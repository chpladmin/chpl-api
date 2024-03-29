package gov.healthit.chpl.domain;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.testtool.TestTool;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TestToolCriteriaMap {
    private Long id;
    private CertificationCriterion criterion;
    private TestTool testTool;
}
