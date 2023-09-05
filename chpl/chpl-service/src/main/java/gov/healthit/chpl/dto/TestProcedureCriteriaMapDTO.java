package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.entity.TestProcedureCriteriaMapEntity;
import lombok.Data;

@Data
public class TestProcedureCriteriaMapDTO implements Serializable {
    private static final long serialVersionUID = -1863384989196377463L;
    private Long id;
    private Long criteriaId;
    private CertificationCriterion criteria;
    private Long testProcedureId;
    private TestProcedureDTO testProcedure;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public TestProcedureCriteriaMapDTO() {
    }

    public TestProcedureCriteriaMapDTO(TestProcedureCriteriaMapEntity entity) {
        this();

        this.id = entity.getId();
        this.criteriaId = entity.getCertificationCriterionId();
        if (entity.getCertificationCriterion() != null) {
            this.criteria = entity.getCertificationCriterion().toDomain();
        }
        this.testProcedureId = entity.getTestProcedureId();
        if (entity.getTestProcedure() != null) {
            this.testProcedure = new TestProcedureDTO(entity.getTestProcedure());
        }

        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }
}
