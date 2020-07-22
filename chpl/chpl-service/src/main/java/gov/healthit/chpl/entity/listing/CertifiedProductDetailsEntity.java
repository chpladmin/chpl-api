package gov.healthit.chpl.entity.listing;

import java.util.Date;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Type;

import gov.healthit.chpl.entity.AttestationType;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.util.Util;

/**
 * Entity containing entirety of a Certified Product.
 * @author alarned
 *
 */
@Entity
@Table(name = "certified_product_details")
public class CertifiedProductDetailsEntity {

    /** Serial Version UID. */
    private static final long serialVersionUID = -2928065796550377879L;

    @Id
    @Basic(optional = false)
    @Column(name = "certified_product_id", nullable = false)
    private Long id;

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

    @Column(name = "chpl_product_number")
    private String chplProductNumber;

    @Column(name = "report_file_location")
    private String reportFileLocation;

    @Column(name = "sed_report_file_location")
    private String sedReportFileLocation;

    @Basic(optional = true)
    @Column(name = "sed_intended_user_description")
    private String sedIntendedUserDescription;

    @Basic(optional = true)
    @Column(name = "meaningful_use_users")
    private Long meaningfulUseUsers;

    @Column(name = "meaningful_use_users_date")
    private Date meaningfulUseUsersDate;

    @Basic(optional = true)
    @Column(name = "sed_testing_end")
    private Date sedTestingEnd;

    @Column(name = "acb_certification_id")
    private String acbCertificationId;

    @Column(name = "practice_type_id")
    private Long practiceTypeId;

    @Column(name = "practice_type_name")
    private String practiceTypeName;

    @Column(name = "product_classification_type_id")
    private Long productClassificationTypeId;

    @Column(name = "other_acb")
    private String otherAcb;

    @Column(name = "certification_status_id")
    private Long certificationStatusId;

    @Column(name = "certification_status_name")
    private String certificationStatusName;

    @Column(name = "last_certification_status_change")
    private Date certificationStatusDate;

    @Column(name = "cures_update")
    private Boolean curesUpdate;

    @Column(name = "certification_edition_id")
    private Long certificationEditionId;

    @Column(name = "year")
    private String year;

    @Column(name = "certification_body_id")
    private Long certificationBodyId;

    @Column(name = "certification_body_name")
    private String certificationBodyName;

    @Column(name = "certification_body_code")
    private String certificationBodyCode;

    @Column(name = "acb_is_retired")
    private Boolean acbIsRetired;

    @Column(name = "product_classification_name")
    private String productClassificationName;

    @Column(name = "product_version_id")
    private Long productVersionId;

    @Column(name = "product_version")
    private String productVersion;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_name")
    private String productName;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", unique = true, nullable = true, insertable = false, updatable = false)
    private ProductEntity product;

    @Column(name = "vendor_id")
    private Long developerId;

    @Column(name = "vendor_name")
    private String developerName;

    @Column(name = "vendor_code")
    private String developerCode;

    @Column(name = "vendor_website")
    private String developerWebsite;

    @Column(name = "vendor_status_id")
    private Long developerStatusId;

    @Column(name = "vendor_status_name")
    private String developerStatusName;

    @Column(name = "last_vendor_status_change")
    private Date developerStatusDate;

    @Column(name = "self_developer")
    private Boolean selfDeveloper;

    @Column(name = "address_id")
    private Long addressId;

    @Column(name = "street_line_1")
    private String streetLine1;

    @Column(name = "street_line_2")
    private String streetLine2;

    @Column(name = "city")
    private String city;

    @Column(name = "state")
    private String state;

    @Column(name = "zipcode")
    private String zipcode;

    @Column(name = "country")
    private String country;

    @Column(name = "contact_id")
    private Long contactId;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "title")
    private String title;

    @Column(name = "creation_date")
    private Date creationDate;

    @Column(name = "certification_date")
    private Date certificationDate;

    @Column(name = "decertification_date")
    private Date decertificationDate;

    @Column(name = "count_certifications")
    private Integer countCertifications;

    @Column(name = "count_cqms")
    private Integer countCqms;

    @Column(name = "count_surveillance_activities")
    private Integer countSurveillance;

    @Column(name = "count_open_surveillance_activities")
    private Integer countOpenSurveillance;

    @Column(name = "count_closed_surveillance_activities")
    private Integer countClosedSurveillance;

    @Column(name = "count_open_nonconformities")
    private Integer countOpenNonconformities;

    @Column(name = "count_closed_nonconformities")
    private Integer countClosedNonconformities;

    @Column(name = "last_modified_date")
    private Date lastModifiedDate;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "ics")
    private Boolean ics;

    @Column(name = "sed")
    private Boolean sedTesting;

    @Column(name = "qms")
    private Boolean qmsTesting;

    @Column(name = "accessibility_certified")
    private Boolean accessibilityCertified;

    @Column(name = "product_additional_software")
    private String productAdditionalSoftware;

    @Column(name = "transparency_attestation")
    @Type(type = "gov.healthit.chpl.entity.PostgresAttestationType", parameters = {
            @org.hibernate.annotations.Parameter(name = "enumClassName",
                    value = "gov.healthit.chpl.entity.AttestationType")
    })
    private AttestationType transparencyAttestation;

    @Column(name = "transparency_attestation_url")
    private String transparencyAttestationUrl;

    @Column(name = "rwt_plan_url")
    private String rwtPlanUrl;

    @Column(name = "rwt_plan_submission_date")
    private Date rwtPlanSubmissionDate;

    @Column(name = "rwt_results_url")
    private String rwtResultsUrl;

    @Column(name = "rwt_results_submission_date")
    private Date rwtResultsSubmissionDate;

    @SuppressWarnings("checkstyle:magicnumber")
    @Transient
    private Integer rwtEligibilityYear = 2021;

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
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

    public String getOtherAcb() {
        return otherAcb;
    }

    public void setOtherAcb(final String otherAcb) {
        this.otherAcb = otherAcb;
    }

    public Long getCertificationStatusId() {
        return certificationStatusId;
    }

    public void setCertificationStatusId(final Long certificationStatusId) {
        this.certificationStatusId = certificationStatusId;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(final String year) {
        this.year = year;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public String getCertificationBodyName() {
        return certificationBodyName;
    }

    public void setCertificationBodyName(final String certificationBodyName) {
        this.certificationBodyName = certificationBodyName;
    }

    public Boolean getAcbIsRetired() {
        return this.acbIsRetired;
    }

    public void setAcbIsRetired(final Boolean acbIsRetired) {
        this.acbIsRetired = acbIsRetired;
    }

    public String getProductClassificationName() {
        return productClassificationName;
    }

    public void setProductClassificationName(final String productClassificationName) {
        this.productClassificationName = productClassificationName;
    }

    public String getPracticeTypeName() {
        return practiceTypeName;
    }

    public void setPracticeTypeName(final String practiceTypeName) {
        this.practiceTypeName = practiceTypeName;
    }

    public Long getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(final Long productVersionId) {
        this.productVersionId = productVersionId;
    }

    public String getProductVersion() {
        return productVersion;
    }

    public void setProductVersion(final String productVersion) {
        this.productVersion = productVersion;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(final Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public Long getDeveloperId() {
        return developerId;
    }

    public void setDeveloperId(final Long developerId) {
        this.developerId = developerId;
    }

    public String getDeveloperName() {
        return developerName;
    }

    public void setDeveloperName(final String developerName) {
        this.developerName = developerName;
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Integer getCountCertifications() {
        return countCertifications;
    }

    public void setCountCertifications(final Integer countCertifications) {
        this.countCertifications = countCertifications;
    }

    public Integer getCountCqms() {
        return countCqms;
    }

    public void setCountCqms(final Integer countCqms) {
        this.countCqms = countCqms;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public String getCertificationStatusName() {
        return certificationStatusName;
    }

    public void setCertificationStatusName(final String certificationStatusName) {
        this.certificationStatusName = certificationStatusName;
    }

    public Boolean getCuresUpdate() {
        return curesUpdate;
    }

    public void setCuresUpdate(Boolean curesUpdate) {
        this.curesUpdate = curesUpdate;
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

    public String getCertificationBodyCode() {
        return certificationBodyCode;
    }

    public void setCertificationBodyCode(final String certificationBodyCode) {
        this.certificationBodyCode = certificationBodyCode;
    }

    public String getDeveloperCode() {
        return developerCode;
    }

    public void setDeveloperCode(final String developerCode) {
        this.developerCode = developerCode;
    }

    public Boolean getSelfDeveloper() {
        return selfDeveloper;
    }

    public void setSelfDeveloper(Boolean selfDeveloper) {
        this.selfDeveloper = selfDeveloper;
    }

    public String getIcsCode() {
        return icsCode;
    }

    public void setIcsCode(final String icsCode) {
        this.icsCode = icsCode;
    }

    public AttestationType getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(final AttestationType transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public Boolean getIcs() {
        return ics;
    }

    public void setIcs(final Boolean ics) {
        this.ics = ics;
    }

    public Boolean getSedTesting() {
        return sedTesting;
    }

    public void setSedTesting(final Boolean sedTesting) {
        this.sedTesting = sedTesting;
    }

    public Boolean getQmsTesting() {
        return qmsTesting;
    }

    public void setQmsTesting(final Boolean qmsTesting) {
        this.qmsTesting = qmsTesting;
    }

    public String getDeveloperWebsite() {
        return developerWebsite;
    }

    public void setDeveloperWebsite(final String developerWebsite) {
        this.developerWebsite = developerWebsite;
    }

    public String getSedReportFileLocation() {
        return sedReportFileLocation;
    }

    public void setSedReportFileLocation(final String sedReportFileLocation) {
        this.sedReportFileLocation = sedReportFileLocation;
    }

    public String getProductAdditionalSoftware() {
        return productAdditionalSoftware;
    }

    public void setProductAdditionalSoftware(final String productAdditionalSoftware) {
        this.productAdditionalSoftware = productAdditionalSoftware;
    }

    public Long getMeaningfulUseUsers() {
        return meaningfulUseUsers;
    }

    public void setMeaningfulUseUsers(final Long meaningfulUseUsers) {
        this.meaningfulUseUsers = meaningfulUseUsers;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(final String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }

    public Boolean getAccessibilityCertified() {
        return accessibilityCertified;
    }

    public void setAccessibilityCertified(final Boolean accessibilityCertified) {
        this.accessibilityCertified = accessibilityCertified;
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

    public Long getAddressId() {
        return addressId;
    }

    public void setAddressId(final Long addressId) {
        this.addressId = addressId;
    }

    public String getStreetLine1() {
        return streetLine1;
    }

    public void setStreetLine1(final String streetLine1) {
        this.streetLine1 = streetLine1;
    }

    public String getStreetLine2() {
        return streetLine2;
    }

    public void setStreetLine2(final String streetLine2) {
        this.streetLine2 = streetLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public String getZipcode() {
        return zipcode;
    }

    public void setZipcode(final String zipcode) {
        this.zipcode = zipcode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) {
        this.country = country;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(final Long contactId) {
        this.contactId = contactId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(final String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(final String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public Long getDeveloperStatusId() {
        return developerStatusId;
    }

    public void setDeveloperStatusId(final Long developerStatusId) {
        this.developerStatusId = developerStatusId;
    }

    public String getDeveloperStatusName() {
        return developerStatusName;
    }

    public void setDeveloperStatusName(final String developerStatusName) {
        this.developerStatusName = developerStatusName;
    }

    public ProductEntity getProduct() {
        return product;
    }

    public void setProduct(final ProductEntity product) {
        this.product = product;
    }

    public Integer getCountSurveillance() {
        return countSurveillance;
    }

    public void setCountSurveillance(final Integer countSurveillance) {
        this.countSurveillance = countSurveillance;
    }

    public Integer getCountOpenSurveillance() {
        return countOpenSurveillance;
    }

    public void setCountOpenSurveillance(final Integer countOpenSurveillance) {
        this.countOpenSurveillance = countOpenSurveillance;
    }

    public Integer getCountClosedSurveillance() {
        return countClosedSurveillance;
    }

    public void setCountClosedSurveillance(final Integer countClosedSurveillance) {
        this.countClosedSurveillance = countClosedSurveillance;
    }

    public Integer getCountOpenNonconformities() {
        return countOpenNonconformities;
    }

    public void setCountOpenNonconformities(final Integer countOpenNonconformities) {
        this.countOpenNonconformities = countOpenNonconformities;
    }

    public Integer getCountClosedNonconformities() {
        return countClosedNonconformities;
    }

    public void setCountClosedNonconformities(final Integer countClosedNonconformities) {
        this.countClosedNonconformities = countClosedNonconformities;
    }

    public Date getCertificationStatusDate() {
        return Util.getNewDate(certificationStatusDate);
    }

    public void setCertificationStatusDate(final Date certificationStatusDate) {
        this.certificationStatusDate = Util.getNewDate(certificationStatusDate);
    }

    public Date getDecertificationDate() {
        return Util.getNewDate(decertificationDate);
    }

    public void setDecertificationDate(final Date decertificationDate) {
        this.decertificationDate = Util.getNewDate(decertificationDate);
    }

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(final Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

    public Date getDeveloperStatusDate() {
        return Util.getNewDate(developerStatusDate);
    }

    public void setDeveloperStatusDate(final Date developerStatusDate) {
        this.developerStatusDate = Util.getNewDate(developerStatusDate);
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getMeaningfulUseUsersDate() {
        return Util.getNewDate(meaningfulUseUsersDate);
    }

    public void setMeaningfulUseUsersDate(Date meaningfulUseUsersDate) {
        this.meaningfulUseUsersDate = Util.getNewDate(meaningfulUseUsersDate);
    }

    public String getRwtPlanUrl() {
        return rwtPlanUrl;
    }

    public void setRwtPlanUrl(String rwtPlanUrl) {
        this.rwtPlanUrl = rwtPlanUrl;
    }

    public Date getRwtPlanSubmissionDate() {
        return rwtPlanSubmissionDate;
    }

    public void setRwtPlanSubmissionDate(Date rwtPlanSubmissionDate) {
        this.rwtPlanSubmissionDate = rwtPlanSubmissionDate;
    }

    public String getRwtResultsUrl() {
        return rwtResultsUrl;
    }

    public void setRwtResultsUrl(String rwtResultsUrl) {
        this.rwtResultsUrl = rwtResultsUrl;
    }

    public Date getRwtResultsSubmissionDate() {
        return rwtResultsSubmissionDate;
    }

    public void setRwtResultsSubmissionDate(Date rwtResultsSubmissionDate) {
        this.rwtResultsSubmissionDate = rwtResultsSubmissionDate;
    }

    public Integer getRwtEligibilityYear() {
        return rwtEligibilityYear;
    }

    public void setRwtEligibilityYear(Integer rwtEligibilityYear) {
        this.rwtEligibilityYear = rwtEligibilityYear;
    }
}
