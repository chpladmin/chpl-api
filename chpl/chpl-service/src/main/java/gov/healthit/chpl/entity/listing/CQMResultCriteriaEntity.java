package gov.healthit.chpl.entity.listing;

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

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "cqm_result_criteria")
public class CQMResultCriteriaEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "cqm_result_criteria_id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "cqm_result_id", nullable = false)
    private Long cqmResultId;

    @Basic(optional = false)
    @Column(name = "certification_criterion_id", nullable = false)
    private Long certificationCriterionId;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", unique = true, nullable = true, insertable = false,
            updatable = false)
    private CertificationCriterionEntity certCriteria;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(nullable = false)
    private Boolean deleted;

    public CQMResultCriteriaEntity() {
        // Default constructor
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    /**
     * Return the value associated with the column: deleted.
     *
     * @return A Boolean object (this.deleted)
     */
    public Boolean isDeleted() {
        return this.deleted;

    }

    /**
     * Set the value related to the column: deleted.
     *
     * @param deleted
     *            the deleted value you wish to set
     */
    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    /**
     * Return the value associated with the column: id.
     *
     * @return A Long object (this.id)
     */
    public Long getId() {
        return this.id;

    }

    /**
     * Set the value related to the column: id.
     *
     * @param id
     *            the id value you wish to set
     */
    public void setId(final Long id) {
        this.id = id;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    /**
     * Return the value associated with the column: lastModifiedUser.
     *
     * @return A Long object (this.lastModifiedUser)
     */
    public Long getLastModifiedUser() {
        return this.lastModifiedUser;

    }

    /**
     * Set the value related to the column: lastModifiedUser.
     *
     * @param lastModifiedUser
     *            the lastModifiedUser value you wish to set
     */
    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public Long getCqmResultId() {
        return cqmResultId;
    }

    public void setCqmResultId(final Long cqmResultId) {
        this.cqmResultId = cqmResultId;
    }

    public Long getCertificationCriterionId() {
        return certificationCriterionId;
    }

    public void setCertificationCriterionId(final Long certificationCriterionId) {
        this.certificationCriterionId = certificationCriterionId;
    }

    public CertificationCriterionEntity getCertCriteria() {
        return certCriteria;
    }

    public void setCertCriteria(final CertificationCriterionEntity certCriteria) {
        this.certCriteria = certCriteria;
    }

}
