package gov.healthit.chpl.domain;

import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.dto.CertificationCriterionDTO;
import gov.healthit.chpl.entity.TestToolCriteriaMapEntity;
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
    private TestTool testTool ;

    public TestToolCriteriaMap(TestToolCriteriaMapEntity entity) {
        this.id = entity.getId();
        if (entity.getTestTool() != null) {
            this.testTool = new TestTool(entity.getTestTool());
        }
        if (entity.getCriteria() != null) {
            this.criterion = new CertificationCriterion(new CertificationCriterionDTO(entity.getCriteria()));
        }
    }
}
