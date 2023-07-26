package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.entity.TestDataCriteriaMapEntity;
import gov.healthit.chpl.util.Util;

public class TestDataCriteriaMapDTO implements Serializable {
    private static final long serialVersionUID = -1863384619196377463L;
    private Long id;
    private Long criteriaId;
    private CertificationCriterion criteria;
    private Long testDataId;
    private TestDataDTO testData;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;

    public TestDataCriteriaMapDTO() {
    }

    public TestDataCriteriaMapDTO(TestDataCriteriaMapEntity entity) {
        this();

        this.id = entity.getId();
        this.criteriaId = entity.getCertificationCriterionId();
        if (entity.getCertificationCriterion() != null) {
            this.criteria = entity.getCertificationCriterion().toDomain();
        }
        this.testDataId = entity.getTestDataId();
        if (entity.getTestData() != null) {
            this.testData = new TestDataDTO(entity.getTestData());
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

    public CertificationCriterion getCriteria() {
        return criteria;
    }

    public void setCriteria(final CertificationCriterion criteria) {
        this.criteria = criteria;
    }

    public Long getTestDataId() {
        return testDataId;
    }

    public void setTestDataId(Long testDataId) {
        this.testDataId = testDataId;
    }

    public TestDataDTO getTestData() {
        return testData;
    }

    public void setTestData(TestDataDTO testData) {
        this.testData = testData;
    }

}
