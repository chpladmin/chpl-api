package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.entity.TestProcedureCriteriaMapEntity;
import gov.healthit.chpl.util.Util;

public class TestProcedureCriteriaMapDTO implements Serializable {
    private static final long serialVersionUID = -1863384989196377463L;
    private Long id;
    private Long criteriaId;
    private CertificationCriterionDTO criteria;
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
            this.criteria = new CertificationCriterionDTO(entity.getCertificationCriterion());
        }
        this.testProcedureId = entity.getTestProcedureId();
        if(entity.getTestProcedure() != null) {
            this.testProcedure = new TestProcedureDTO(entity.getTestProcedure());
        }

        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }
    
    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Long getCriteriaId() {
        return criteriaId;
    }

    public void setCriteriaId(final Long criteriaId) {
        this.criteriaId = criteriaId;
    }

    public CertificationCriterionDTO getCriteria() {
        return criteria;
    }

    public void setCriteria(final CertificationCriterionDTO criteria) {
        this.criteria = criteria;
    }

    public Long getTestProcedureId() {
        return testProcedureId;
    }

    public void setTestProcedureId(Long testProcedureId) {
        this.testProcedureId = testProcedureId;
    }

    public TestProcedureDTO getTestProcedure() {
        return testProcedure;
    }

    public void setTestProcedure(TestProcedureDTO testProcedure) {
        this.testProcedure = testProcedure;
    }

}
