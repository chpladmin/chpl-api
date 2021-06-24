package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.TestFunctionalityCriteriaMapEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TestFunctionalityCriteriaMapDTO {
    private Long id;
    private CertificationCriterionDTO criteria;
    private TestFunctionalityDTO testFunctionality;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public TestFunctionalityCriteriaMapDTO(TestFunctionalityCriteriaMapEntity entity) {
        this();

        this.id = entity.getId();
        if (entity.getCriteria() != null) {
            this.criteria = new CertificationCriterionDTO(entity.getCriteria());
        }
        if (entity.getTestFunctionality() != null) {
            this.testFunctionality = new TestFunctionalityDTO(entity.getTestFunctionality());
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }
}
