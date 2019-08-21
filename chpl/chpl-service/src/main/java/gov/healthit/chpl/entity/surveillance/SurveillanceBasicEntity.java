package gov.healthit.chpl.entity.surveillance;

import java.util.Date;

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

import gov.healthit.chpl.util.Util;

@Entity
@Table(name = "surveillance_basic")
public class SurveillanceBasicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "friendly_id", insertable = false, updatable = false)
    private String friendlyId;

    @Column(name = "certified_product_id")
    private Long certifiedProductId;

    @Column(name = "start_date")
    private Date startDate;

    @Column(name = "end_date")
    private Date endDate;

    @Column(name = "type_id")
    private Long surveillanceTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", insertable = false, updatable = false)
    private SurveillanceTypeEntity surveillanceType;

    @Column(name = "randomized_sites_used")
    private Integer numRandomizedSites;

    @Column(name = "open_nonconformity_count")
    private Integer numOpenNonconformities;

    @Column(name = "closed_nonconformity_count")
    private Integer numClosedNonconformities;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @Column(name = "user_permission_id")
    private Long userPermissionId;

    @Transient
    private String chplProductNumber;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Date getStartDate() {
        return Util.getNewDate(startDate);
    }

    public void setStartDate(final Date startDate) {
        this.startDate = Util.getNewDate(startDate);
    }

    public Date getEndDate() {
        return Util.getNewDate(endDate);
    }

    public void setEndDate(final Date endDate) {
        this.endDate = Util.getNewDate(endDate);
    }

    public Integer getNumRandomizedSites() {
        return numRandomizedSites;
    }

    public void setNumRandomizedSites(final Integer numRandomizedSites) {
        this.numRandomizedSites = numRandomizedSites;
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

    public Long getCertifiedProductId() {
        return certifiedProductId;
    }

    public void setCertifiedProductId(final Long certifiedProductId) {
        this.certifiedProductId = certifiedProductId;
    }

    public Long getSurveillanceTypeId() {
        return surveillanceTypeId;
    }

    public void setSurveillanceTypeId(final Long surveillanceTypeId) {
        this.surveillanceTypeId = surveillanceTypeId;
    }

    public SurveillanceTypeEntity getSurveillanceType() {
        return surveillanceType;
    }

    public void setSurveillanceType(final SurveillanceTypeEntity surveillanceType) {
        this.surveillanceType = surveillanceType;
    }

    public String getFriendlyId() {
        return friendlyId;
    }

    public void setFriendlyId(final String friendlyId) {
        this.friendlyId = friendlyId;
    }

    public Long getUserPermissionId() {
        return userPermissionId;
    }

    public void setUserPermissionId(final Long userPermissionId) {
        this.userPermissionId = userPermissionId;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    @Override
    public String toString() {
        return "SurveillanceBasicEntity [id=" + id + ", friendlyId=" + friendlyId + ", certifiedProductId="
                + certifiedProductId + ", startDate=" + startDate + ", endDate=" + endDate + ", surveillanceTypeId="
                + surveillanceTypeId + ", surveillanceType=" + surveillanceType + ", numRandomizedSites="
                + numRandomizedSites + ", deleted=" + deleted + ", lastModifiedUser=" + lastModifiedUser
                + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate + ", userPermissionId="
                + userPermissionId + ", chplProductNumber=" + chplProductNumber + "]";
    }

    public Integer getNumOpenNonconformities() {
        return numOpenNonconformities;
    }

    public void setNumOpenNonconformities(final Integer numOpenNonconformities) {
        this.numOpenNonconformities = numOpenNonconformities;
    }

    public Integer getNumClosedNonconformities() {
        return numClosedNonconformities;
    }

    public void setNumClosedNonconformities(final Integer numClosedNonconformities) {
        this.numClosedNonconformities = numClosedNonconformities;
    }
}
