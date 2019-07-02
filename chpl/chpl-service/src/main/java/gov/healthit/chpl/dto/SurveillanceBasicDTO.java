package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.SurveillanceBasic;
import gov.healthit.chpl.entity.surveillance.SurveillanceBasicEntity;
import gov.healthit.chpl.util.Util;

public class SurveillanceBasicDTO implements Serializable {
    private static final long serialVersionUID = -2434007762463213735L;

    private Long id;
    private String friendlyId;
    private Long certifiedProductId;
    private Date startDate;
    private Date endDate;
    private Long surveillanceTypeId;
    private SurveillanceTypeDTO surveillanceType;
    private Integer numRandomizedSites;
    private Boolean deleted;
    private Long lastModifiedUser;
    private Date creationDate;
    private Date lastModifiedDate;
    private Long userPermissionId;
    private String chplProductNumber;

    public SurveillanceBasicDTO() {

    }

    public SurveillanceBasicDTO(SurveillanceBasicEntity entity) {
        BeanUtils.copyProperties(entity, this);
        if (entity.getSurveillanceType() != null) {
            this.surveillanceType = new SurveillanceTypeDTO(entity.getSurveillanceType());
        }
    }

    public SurveillanceBasicDTO(SurveillanceBasic domain) {
        BeanUtils.copyProperties(domain, this);
        if (domain.getSurveillanceType() != null) {
            this.surveillanceType = new SurveillanceTypeDTO(domain.getSurveillanceType());
        }
    }

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

    public SurveillanceTypeDTO getSurveillanceType() {
        return surveillanceType;
    }

    public void setSurveillanceType(final SurveillanceTypeDTO surveillanceType) {
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
        return "SurveillanceBasicDTO [id=" + id + ", friendlyId=" + friendlyId + ", certifiedProductId="
                + certifiedProductId + ", startDate=" + startDate + ", endDate=" + endDate + ", surveillanceTypeId="
                + surveillanceTypeId + ", surveillanceType=" + surveillanceType + ", numRandomizedSites="
                + numRandomizedSites + ", deleted=" + deleted + ", lastModifiedUser=" + lastModifiedUser
                + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate + ", userPermissionId="
                + userPermissionId + ", chplProductNumber=" + chplProductNumber + "]";
    }
}
