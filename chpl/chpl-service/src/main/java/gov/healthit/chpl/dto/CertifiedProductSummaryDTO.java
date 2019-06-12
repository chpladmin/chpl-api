package gov.healthit.chpl.dto;

import java.util.Date;

import org.springframework.beans.BeanUtils;

import gov.healthit.chpl.domain.CertifiedProductSummary;
import gov.healthit.chpl.entity.listing.CertifiedProductSummaryEntity;
import gov.healthit.chpl.util.Util;

public class CertifiedProductSummaryDTO {
    private Long id;
    private Long certificationEditionId;
    private Long productVersionId;
    private Long certificationBodyId;
    private String chplProductNumber;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    private Date sedTestingEnd;
    private Long acbCertificationId;
    private Long practiceTypeId;
    private Long productClassificationTypeId;
    private String productAdditionalSoftware;
    private String otherAcb;
    private String transparencyAttestationUrl;
    private Boolean ics;
    private Boolean sed;
    private Boolean qms;
    private Boolean accessibilityCertified;
    private String productCode;
    private String versionCode;
    private String icsCode;
    private String additionalSoftwareCode;
    private String certifiedDateCode;
    private Date creationDate;
    private Date lastModifiedDate;
    private String lastModifiedUser;
    private Boolean deleted;
    private Long meaninigfulUseUsers;
    private Long pendingCertifiedProductId;
    private String year;
    private String productName;
    private String developerName;
    private String developerCode;
    private String certificationStatus;
    private String acbCode;
    private String certificationBodyName;
    private String certificationBodyWebsite;

    public CertifiedProductSummaryDTO() {

    }

    public CertifiedProductSummaryDTO(CertifiedProductSummaryEntity entity) {
        BeanUtils.copyProperties(entity, this);
    }

    public CertifiedProductSummaryDTO(CertifiedProductSummary domain) {
        BeanUtils.copyProperties(domain, this);
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public Long getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(final Long productVersionId) {
        this.productVersionId = productVersionId;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public void setReportFileLocation(final String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    public String getSedReportFileLocation() {
        return sedReportFileLocation;
    }

    public void setSedReportFileLocation(final String sedReportFileLocation) {
        this.sedReportFileLocation = sedReportFileLocation;
    }

    public String getSedIntendedUserDescription() {
        return sedIntendedUserDescription;
    }

    public void setSedIntendedUserDescription(final String sedIntendedUserDescription) {
        this.sedIntendedUserDescription = sedIntendedUserDescription;
    }

    public Date getSedTestingEnd() {
        return Util.getNewDate(sedTestingEnd);
    }

    public void setSedTestingEnd(final Date sedTestingEnd) {
        this.sedTestingEnd = Util.getNewDate(sedTestingEnd);
    }

    public Long getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(final Long acbCertificationId) {
        this.acbCertificationId = acbCertificationId;
    }

    public Long getPracticeTypeId() {
        return practiceTypeId;
    }

    public void setPracticeTypeId(final Long practiceTypeId) {
        this.practiceTypeId = practiceTypeId;
    }

    public Long getProductClassificationTypeId() {
        return productClassificationTypeId;
    }

    public void setProductClassificationTypeId(final Long productClassificationTypeId) {
        this.productClassificationTypeId = productClassificationTypeId;
    }

    public String getProductAdditionalSoftware() {
        return productAdditionalSoftware;
    }

    public void setProductAdditionalSoftware(final String productAdditionalSoftware) {
        this.productAdditionalSoftware = productAdditionalSoftware;
    }

    public String getOtherAcb() {
        return otherAcb;
    }

    public void setOtherAcb(final String otherAcb) {
        this.otherAcb = otherAcb;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(final String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }

    public Boolean getIcs() {
        return ics;
    }

    public void setIcs(final Boolean ics) {
        this.ics = ics;
    }

    public Boolean getSed() {
        return sed;
    }

    public void setSed(final Boolean sed) {
        this.sed = sed;
    }

    public Boolean getQms() {
        return qms;
    }

    public void setQms(final Boolean qms) {
        this.qms = qms;
    }

    public Boolean getAccessibilityCertified() {
        return accessibilityCertified;
    }

    public void setAccessibilityCertified(final Boolean accessibilityCertified) {
        this.accessibilityCertified = accessibilityCertified;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(final String productCode) {
        this.productCode = productCode;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(final String versionCode) {
        this.versionCode = versionCode;
    }

    public String getIcsCode() {
        return icsCode;
    }

    public void setIcsCode(final String icsCode) {
        this.icsCode = icsCode;
    }

    public String getAdditionalSoftwareCode() {
        return additionalSoftwareCode;
    }

    public void setAdditionalSoftwareCode(final String additionalSoftwareCode) {
        this.additionalSoftwareCode = additionalSoftwareCode;
    }

    public String getCertifiedDateCode() {
        return certifiedDateCode;
    }

    public void setCertifiedDateCode(final String certifiedDateCode) {
        this.certifiedDateCode = certifiedDateCode;
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

    public String getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final String lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Long getMeaninigfulUseUsers() {
        return meaninigfulUseUsers;
    }

    public void setMeaninigfulUseUsers(final Long meaninigfulUseUsers) {
        this.meaninigfulUseUsers = meaninigfulUseUsers;
    }

    public Long getPendingCertifiedProductId() {
        return pendingCertifiedProductId;
    }

    public void setPendingCertifiedProductId(final Long pendingCertifiedProductId) {
        this.pendingCertifiedProductId = pendingCertifiedProductId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(final String developerName) {
        this.developerName = developerName;
    }

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(final String developerCode) {
        this.developerCode = developerCode;
    }

    public String getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(final String certificationStatus) {
        this.certificationStatus = certificationStatus;
    }

    public String getAcbCode() {
        return acbCode;
    }

    public void setAcbCode(final String acbCode) {
        this.acbCode = acbCode;
    }

    public String getCertificationBodyName() {
        return certificationBodyName;
    }

    public void setCertificationBodyName(final String certificationBodyName) {
        this.certificationBodyName = certificationBodyName;
    }

    public String getCertificationBodyWebsite() {
        return certificationBodyWebsite;
    }

    public void setCertificationBodyWebsite(final String certificationBodyWebsite) {
        this.certificationBodyWebsite = certificationBodyWebsite;
    }

    @Override
    public String toString() {
        return "CertifiedProductSummaryDTO [id=" + id + ", certificationEditionId=" + certificationEditionId
                + ", productVersionId=" + productVersionId + ", certificationBodyId=" + certificationBodyId
                + ", chplProductNumber=" + chplProductNumber + ", reportFileLocation=" + reportFileLocation
                + ", sedReportFileLocation=" + sedReportFileLocation + ", sedIntendedUserDescription="
                + sedIntendedUserDescription + ", sedTestingEnd=" + sedTestingEnd + ", acbCertificationId="
                + acbCertificationId + ", practiceTypeId=" + practiceTypeId + ", productClassificationTypeId="
                + productClassificationTypeId + ", productAdditionalSoftware=" + productAdditionalSoftware
                + ", otherAcb=" + otherAcb + ", transparencyAttestationUrl=" + transparencyAttestationUrl + ", ics="
                + ics + ", sed=" + sed + ", qms=" + qms + ", accessibilityCertified=" + accessibilityCertified
                + ", productCode=" + productCode + ", versionCode=" + versionCode + ", icsCode=" + icsCode
                + ", additionalSoftwareCode=" + additionalSoftwareCode + ", certifiedDateCode=" + certifiedDateCode
                + ", creationDate=" + creationDate + ", lastModifiedDate=" + lastModifiedDate + ", lastModifiedUser="
                + lastModifiedUser + ", deleted=" + deleted + ", meaninigfulUseUsers=" + meaninigfulUseUsers
                + ", pendingCertifiedProductId=" + pendingCertifiedProductId + ", year=" + year + ", productName="
                + productName + ", developerName=" + developerName + ", developerCode=" + developerCode
                + ", certificationStatus=" + certificationStatus + ", acbCode=" + acbCode + ", certificationBodyName="
                + certificationBodyName + ", certificationBodyWebsite=" + certificationBodyWebsite + "]";
    }
}
