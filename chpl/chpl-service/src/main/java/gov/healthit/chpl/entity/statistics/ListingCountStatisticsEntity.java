package gov.healthit.chpl.entity.statistics;

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
import javax.persistence.Transient;

import gov.healthit.chpl.entity.CertificationEditionEntity;
import gov.healthit.chpl.entity.CertificationStatusEntity;
import gov.healthit.chpl.util.Util;

/**
 * Entity object representing the listing_count_statistics table.
 * @author alarned
 *
 */
@Entity
@Table(name = "listing_count_statistics")
public class ListingCountStatisticsEntity implements Serializable {
    private static final long serialVersionUID = 1313677047965534572L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic(optional = false)
    @Column(name = "developer_count", nullable = false)
    private Long developerCount;

    @Basic(optional = false)
    @Column(name = "product_count", nullable = false)
    private Long productCount;

    @Basic(optional = false)
    @Column(name = "certification_edition_id", nullable = false)
    private Long certificationEditionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_edition_id", insertable = false, updatable = false)
    private CertificationEditionEntity certificationEdition;

    @Basic(optional = false)
    @Column(name = "certification_status_id", nullable = false)
    private Long certificationStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_status_id", insertable = false, updatable = false)
    private CertificationStatusEntity certificationStatus;

    @Basic(optional = false)
    @Column(name = "creation_date", nullable = false)
    private Date creationDate;

    @Basic(optional = false)
    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Basic(optional = false)
    @Column(name = "last_modified_date", nullable = false)
    private Date lastModifiedDate;

    @Basic(optional = false)
    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    /**
     * Default constructor.
     */
    public ListingCountStatisticsEntity() {
        this.developerCount = 0L;
        this.productCount = 0L;
    }

    /**
     * Sets the id field upon creation.
     * @param id The value to set object's id equal to
     */
    public ListingCountStatisticsEntity(final Long id) {
        this.id = id;
    }

    @Transient
    public Class<?> getClassType() {
        return ListingCountStatisticsEntity.class;
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getDeveloperCount() {
        return developerCount;
    }

    public void setDeveloperCount(final Long developerCount) {
        this.developerCount = developerCount;
    }

    public Long getProductCount() {
        return productCount;
    }

    public void setProductCount(final Long productCount) {
        this.productCount = productCount;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public CertificationEditionEntity getCertificationEdition() {
        return certificationEdition;
    }

    public void setCertificationEdition(final CertificationEditionEntity certificationEdition) {
        this.certificationEdition = certificationEdition;
    }

    public Long getCertificationStatusId() {
        return certificationStatusId;
    }

    public void setCertificationStatusId(final Long certificationStatusId) {
        this.certificationStatusId = certificationStatusId;
    }

    public CertificationStatusEntity getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final CertificationStatusEntity certificationStatus) {
        this.certificationStatus = certificationStatus;
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

    @Override
    public String toString() {
        return "Incumbent Developers Statistics Entity ["
                + "[Developer: " + this.developerCount + "]"
                + "[Product: " + this.productCount + "]"
                + "[Edition: " + this.certificationEdition.getYear() + "]"
                + "[Status: " + this.certificationStatus.getStatus() + "]"
                + "]";
    }
}
