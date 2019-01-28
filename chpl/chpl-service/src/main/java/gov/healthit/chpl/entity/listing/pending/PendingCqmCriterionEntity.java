package gov.healthit.chpl.entity.listing.pending;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.CQMCriterionEntity;
import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "pending_cqm_criterion")
public class PendingCqmCriterionEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pending_cqm_criterion_id", nullable = false)
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "cqm_criterion_id", unique = true, nullable = true)
    private CQMCriterionEntity mappedCriterion;

    @Column(name = "pending_certified_product_id")
    private Long pendingCertifiedProductId;

    @Column(name = "meets_criteria")
    private Boolean meetsCriteria;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingCqmId")
    @Basic(optional = false)
    @Column(name = "pending_cqm_criterion_id", nullable = false)
    private Set<PendingCqmCertificationCriteriaEntity> certifications = new HashSet<PendingCqmCertificationCriteriaEntity>();

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getPendingCertifiedProductId() {
        return pendingCertifiedProductId;
    }

    public void setPendingCertifiedProductId(final Long pendingCertifiedProductId) {
        this.pendingCertifiedProductId = pendingCertifiedProductId;
    }

    public Boolean getMeetsCriteria() {
        return meetsCriteria;
    }

    public void setMeetsCriteria(final Boolean meetsCriteria) {
        this.meetsCriteria = meetsCriteria;
    }

    // public String getCqmNumber() {
    // return cqmNumber;
    // }
    //
    // public void setCqmNumber(final String cqmNumber) {
    // this.cqmNumber = cqmNumber;
    // }
    //
    // public String getCmsId() {
    // return cmsId;
    // }
    //
    // public void setCmsId(final String cmsId) {
    // this.cmsId = cmsId;
    // }
    //
    // public String getTitle() {
    // return title;
    // }
    //
    // public void setTitle(final String title) {
    // this.title = title;
    // }
    //
    // public String getNqfNumber() {
    // return nqfNumber;
    // }
    //
    // public void setNqfNumber(final String nqfNumber) {
    // this.nqfNumber = nqfNumber;
    // }
    //
    // public String getVersion() {
    // return version;
    // }
    //
    // public void setVersion(final String version) {
    // this.version = version;
    // }

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

    public CQMCriterionEntity getMappedCriterion() {
        return mappedCriterion;
    }

    public void setMappedCriterion(final CQMCriterionEntity mappedCriterion) {
        this.mappedCriterion = mappedCriterion;
    }

    public Set<PendingCqmCertificationCriteriaEntity> getCertifications() {
        return certifications;
    }

    public void setCertifications(final Set<PendingCqmCertificationCriteriaEntity> certifications) {
        this.certifications = certifications;
    }
}
