package gov.healthit.chpl.entity.listing;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.util.Util;

@Entity
@Immutable
@Table(name = "certified_product_summary")
public class CertifiedProductSummaryEntity implements Serializable {
    private static final long serialVersionUID = -7006206379019745873L;

    @Id
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Column(name = "product_version_id")
    private Long productVersionId;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "report_file_location")
    private String reportFileLocation;

    @Column(name = "sed_report_file_location")
    private String sedReportFileLocation;

    @Column(name = "sed_intended_user_description")
    private String sedIntendedUserDescription;

    @Column(name = "sed_testing_end")
    private Date sedTestingEnd;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "practice_type_id")
    private Long practiceTypeId;

    @Column(name = "product_classification_type_id")
    private Long productClassificationTypeId;

    @Column(name = "product_additional_software")
    private String productAdditionalSoftware;

    @Column(name = "other_acb")
    private String otherAcb;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "ics")
    private Boolean ics;

    @Column(name = "sed")
    private Boolean sed;

    @Column(name = "qms")
    private Boolean qms;

    @Column(name = "accessibility_certified")
    private Boolean accessibilityCertified;

    @Column(name = "product_code")
    private String productCode;

    @Column(name = "version_code")
    private String versionCode;

    @Column(name = "ics_code")
    private String icsCode;

    @Column(name = "additional_software_code")
    private String additionalSoftwareCode;

    @Column(name = "certified_date_code")
    private String certifiedDateCode;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "last_modified_user")
    private String lastModifiedUser;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "meaningful_use_users")
    private Long meaninigfulUseUsers;

    @Column(name = "pending_certified_product_id")
    private Long pendingCertifiedProductId;

    @Column(name = "year")
    private String year;

    @Column(name = "product_name")
    private String productName;

    @Column(name = "vendor_name")
    private String developerName;

    @Column(name = "vendor_code")
    private String developerCode;

    @Column(name = "certification_status")
    private String certificationStatus;

    @Column(name = "acb_code")
    private String acbCode;

    @Column(name = "certification_body_name")
    private String certificationBodyName;

    @Column(name = "certification_body_website")
    private String certificationBodyWebsite;

    @Column(name = "version")
    private String version;

    @Column(name = "full_name")
    private String developerContactName;

    @Column(name = "email")
    private String developerContactEmail;

    @Column(name = "phone_number")
    private String developerContactPhone;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "certifiedProductId")
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<CertificationResultDetailsEntity> certificationResults = new HashSet<CertificationResultDetailsEntity>();


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

    public String getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(final String acbCertificationId) {
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

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getDeveloperContactName() {
        return developerContactName;
    }

    public void setDeveloperContactName(final String developerContactName) {
        this.developerContactName = developerContactName;
    }

    public String getDeveloperContactEmail() {
        return developerContactEmail;
    }

    public void setDeveloperContactEmail(final String developerContactEmail) {
        this.developerContactEmail = developerContactEmail;
    }

    public String getDeveloperContactPhone() {
        return developerContactPhone;
    }

    public void setDeveloperContactPhone(final String developerContactPhone) {
        this.developerContactPhone = developerContactPhone;
    }

    public Set<CertificationResultDetailsEntity> getCertificationResults() {
        return certificationResults;
    }

    public void setCertificationResults(final Set<CertificationResultDetailsEntity> certificationResults) {
        this.certificationResults = certificationResults;
    }

}
