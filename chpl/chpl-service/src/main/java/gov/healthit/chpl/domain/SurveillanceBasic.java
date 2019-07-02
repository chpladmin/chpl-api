package gov.healthit.chpl.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.dto.SurveillanceBasicDTO;
import gov.healthit.chpl.util.Util;

public class SurveillanceBasic implements Serializable {
    private static final long serialVersionUID = 3750079664886758825L;

    private Long id;
    private String friendlyId;
    private Long certifiedProductId;
    private Date startDate;
    private Date endDate;
    private Long surveillanceTypeId;
    private SurveillanceType surveillanceType;
    private Integer numRandomizedSites;
    private Long userPermissionId;
    private String chplProductNumber;

    public SurveillanceBasic() {

    }

    public SurveillanceBasic(SurveillanceBasicDTO dto) {
        BeanUtils.copyProperties(dto, this);
        this.surveillanceType = new SurveillanceType(dto.getSurveillanceType());
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

    public SurveillanceType getSurveillanceType() {
        return surveillanceType;
    }

    public void setSurveillanceType(final SurveillanceType surveillanceType) {
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
        return "SurveillanceBasic [id=" + id + ", friendlyId=" + friendlyId + ", certifiedProductId="
                + certifiedProductId + ", startDate=" + startDate + ", endDate=" + endDate + ", surveillanceTypeId="
                + surveillanceTypeId + ", surveillanceType=" + surveillanceType + ", numRandomizedSites="
                + numRandomizedSites + ", userPermissionId=" + userPermissionId + ", chplProductNumber="
                + chplProductNumber + "]";
    }
}
