package gov.healthit.chpl.entity;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "test_data_criteria_map")
public class TestDataCriteriaMapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "criteria_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    @Column(name = "test_data_id")
    private Long testDataId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_data_id", insertable = false, updatable = false)
    private TestDataEntity testData;

    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    protected Date creationDate;

    @Column(nullable = false)
    protected Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    protected Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    protected Long lastModifiedUser;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
    }

    public CertificationCriterionEntity getCertificationCriterion() {
        return certificationCriterion;
    }

    public void setCertificationCriterion(CertificationCriterionEntity certificationCriterion) {
        this.certificationCriterion = certificationCriterion;
    }

    public Long getTestDataId() {
        return testDataId;
    }

    public void setTestDataId(Long testDataId) {
        this.testDataId = testDataId;
    }

    public TestDataEntity getTestData() {
        return testData;
    }

    public void setTestData(TestDataEntity testData) {
        this.testData = testData;
    }
}
