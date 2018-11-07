package gov.healthit.chpl.dto;

import java.util.Date;

import gov.healthit.chpl.entity.TestFunctionalityCriteriaMapEntity;

public class TestFunctionalityCriteriaMapDTO {
    private Long id;
    private CertificationCriterionDTO criteria;
    private TestFunctionalityDTO testFunctionality;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public TestFunctionalityCriteriaMapDTO() {
    }

    public TestFunctionalityCriteriaMapDTO(TestFunctionalityCriteriaMapEntity entity) {
        this();

        this.id = entity.getId();
        if (entity.getCriteria() != null) {
            this.criteria = new CertificationCriterionDTO(entity.getCriteria());
        }
        if(entity.getTestFunctionality() != null) {
            this.testFunctionality = new TestFunctionalityDTO(entity.getTestFunctionality());
        }
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
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
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public CertificationCriterionDTO getCriteria() {
        return criteria;
    }

    public void setCriteria(final CertificationCriterionDTO criteria) {
        this.criteria = criteria;
    }

    public TestFunctionalityDTO getTestFunctionality() {
        return testFunctionality;
    }

    public void setTestFunctionality(TestFunctionalityDTO testFunctionality) {
        this.testFunctionality = testFunctionality;
    }

}
