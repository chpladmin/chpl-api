package gov.healthit.chpl.entity;

import java.io.Serializable;
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

import gov.healthit.chpl.util.Util;

/**
 * @author TYoung
 * Handles the mapping many-to-many relationship between test_functionality and
 * certification_criterion table.
 *
 */
@Entity
@Table(name = "test_functionality_criteria_map")
public class TestFunctionalityCriteriaMapEntity implements Serializable {
    private static final long serialVersionUID = 6446486138564063907L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "criteria_id")
    private Long certificationCriterionId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", insertable = false, updatable = false)
    private CertificationCriterionEntity criteria;

    @Column(name = "test_functionality_id")
    private Long testFunctionalityId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "test_functionality_id", insertable = false, updatable = false)
    private TestFunctionalityEntity testFunctionality;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CertificationCriterionEntity getCriteria() {
        return criteria;
    }

    public void setCriteria(CertificationCriterionEntity criteria) {
        this.criteria = criteria;
    }

    public TestFunctionalityEntity getTestFunctionality() {
        return testFunctionality;
    }

    public void setTestFunctionality(TestFunctionalityEntity testFunctionality) {
        this.testFunctionality = testFunctionality;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
    }

    public Long getTestFunctionalityId() {
        return testFunctionalityId;
    }

    public void setTestFunctionalityId(Long testFunctionalityId) {
        this.testFunctionalityId = testFunctionalityId;
    }
}
