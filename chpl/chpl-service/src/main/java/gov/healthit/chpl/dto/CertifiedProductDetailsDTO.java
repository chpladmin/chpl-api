package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Date;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntitySimple;
import gov.healthit.chpl.util.Util;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class CertifiedProductDetailsDTO implements Serializable {
    private static final long serialVersionUID = 6238278848984479683L;
    private Long id;
    private String productCode;
    private String versionCode;
    private String icsCode;
    private String additionalSoftwareCode;
    private String certifiedDateCode;
    private Long testingLabId;
    private String testingLabName;
    private String testingLabCode;
    private String chplProductNumber;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date sedTestingEnd;
    private String acbCertificationId;
    private Long practiceTypeId;
    private String practiceTypeName;
    private Long productClassificationTypeId;
    private String otherAcb;
    private Long certificationStatusId;
    private String certificationStatusName;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date certificationStatusDate;
    private Boolean curesUpdate;
    private Long certificationEditionId;
    private String year;
    private Long certificationBodyId;
    private String certificationBodyName;
    private String certificationBodyCode;
    private String productClassificationName;
    private DeveloperDTO developer;
    private DeveloperStatusEventDTO developerCurrentStatus;
    private ProductDTO product;
    private ProductVersionDTO version;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date creationDate;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date certificationDate;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date decertificationDate;
    private Integer countCertifications;
    private Integer countCqms;
    private Integer countSurveillance;
    private Integer countOpenSurveillance;
    private Integer countClosedSurveillance;
    private Integer countOpenNonconformities;
    private Integer countClosedNonconformities;
    @Setter(AccessLevel.NONE)
    @Getter(AccessLevel.NONE)
    private Date lastModifiedDate;
    private Boolean ics;
    private Boolean sedTesting;
    private Boolean qmsTesting;
    private Boolean accessibilityCertified;
    private String productAdditionalSoftware;
    private TransparencyAttestationDTO transparencyAttestation;
    private String mandatoryDisclosures;
    private String rwtPlansUrl;
    private LocalDate rwtPlansCheckDate;
    private String rwtResultsUrl;
    private LocalDate rwtResultsCheckDate;
    private Integer rwtEligibilityYear;
    private String svapNoticeUrl;

    private static final int FOUR_DIGIT_YEAR = 4;

    public CertifiedProductDetailsDTO(CertifiedProductDetailsEntity entity) {
        this();

        this.id = entity.getId();
        this.productCode = entity.getProductCode();
        this.versionCode = entity.getVersionCode();
        this.icsCode = entity.getIcsCode();
        this.additionalSoftwareCode = entity.getAdditionalSoftwareCode();
        this.creationDate = entity.getCreationDate();
        this.certifiedDateCode = entity.getCertifiedDateCode();
        this.acbCertificationId = entity.getAcbCertificationId();
        this.certificationBodyId = entity.getCertificationBodyId();
        this.certificationBodyName = entity.getCertificationBodyName();
        this.certificationBodyCode = entity.getCertificationBodyCode();
        this.certificationEditionId = entity.getCertificationEditionId();
        this.certificationStatusId = entity.getCertificationStatusId();
        this.certificationStatusName = entity.getCertificationStatusName();
        this.certificationStatusDate = entity.getCertificationStatusDate();
        this.curesUpdate = entity.getCuresUpdate();
        this.chplProductNumber = entity.getChplProductNumber();
        this.otherAcb = entity.getOtherAcb();
        this.practiceTypeId = entity.getPracticeTypeId();
        this.practiceTypeName = entity.getPracticeTypeName();
        this.productClassificationName = entity.getProductClassificationName();
        this.productClassificationTypeId = entity.getProductClassificationTypeId();
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.sedIntendedUserDescription = entity.getSedIntendedUserDescription();
        this.sedTestingEnd = entity.getSedTestingEnd();

        this.developer = new DeveloperDTO();
        this.developer.setId(entity.getDeveloperId());
        this.developer.setName(entity.getDeveloperName());
        this.developer.setDeveloperCode(entity.getDeveloperCode());
        this.developer.setWebsite(entity.getDeveloperWebsite());
        this.developer.setSelfDeveloper(entity.getSelfDeveloper());

        if (entity.getDeveloperStatusId() != null) {
            developerCurrentStatus = new DeveloperStatusEventDTO();
            developerCurrentStatus.setDeveloperId(entity.getDeveloperId());
            DeveloperStatusDTO statusObj = new DeveloperStatusDTO();
            statusObj.setId(entity.getDeveloperStatusId());
            statusObj.setStatusName(entity.getDeveloperStatusName());
            developerCurrentStatus.setStatus(statusObj);
            developerCurrentStatus.setStatusDate(entity.getDeveloperStatusDate());
            this.developer.getStatusEvents().add(developerCurrentStatus);
        }

        if (entity.getAddressId() != null) {
            AddressDTO developerAddress = new AddressDTO();
            developerAddress.setId(entity.getAddressId());
            developerAddress.setStreetLineOne(entity.getStreetLine1());
            developerAddress.setStreetLineTwo(entity.getStreetLine2());
            developerAddress.setCity(entity.getCity());
            developerAddress.setState(entity.getState());
            developerAddress.setZipcode(entity.getZipcode());
            developerAddress.setCountry(entity.getCountry());
            this.developer.setAddress(developerAddress);
        }
        if (entity.getContactId() != null) {
            ContactDTO developerContact = new ContactDTO();
            developerContact.setId(entity.getContactId());
            developerContact.setFullName(entity.getFullName());
            developerContact.setEmail(entity.getEmail());
            developerContact.setPhoneNumber(entity.getPhoneNumber());
            developerContact.setTitle(entity.getTitle());
            this.developer.setContact(developerContact);
        }

        if (entity.getProduct() != null) {
            this.product = new ProductDTO(entity.getProduct());
        }

        if (entity.getProductVersionId() != null) {
            this.version = new ProductVersionDTO();
            this.version.setId(entity.getProductVersionId());
            this.version.setVersion(entity.getProductVersion());
        }

        this.ics = entity.getIcs();
        this.sedTesting = entity.getSedTesting();
        this.qmsTesting = entity.getQmsTesting();
        this.accessibilityCertified = entity.getAccessibilityCertified();
        this.productAdditionalSoftware = entity.getProductAdditionalSoftware();
        if (entity.getTransparencyAttestation() != null) {
            this.transparencyAttestation = new TransparencyAttestationDTO(entity.getTransparencyAttestation().toString());
        }
        this.mandatoryDisclosures = entity.getMandatoryDisclosures();
        this.year = entity.getYear();
        this.certificationDate = entity.getCertificationDate();
        this.decertificationDate = entity.getDecertificationDate();
        this.countCqms = entity.getCountCqms();
        this.countCertifications = entity.getCountCertifications();
        this.countSurveillance = entity.getCountSurveillance();
        this.countOpenSurveillance = entity.getCountOpenSurveillance();
        this.countClosedSurveillance = entity.getCountClosedSurveillance();
        this.countOpenNonconformities = entity.getCountOpenNonconformities();
        this.countClosedNonconformities = entity.getCountClosedNonconformities();
        this.rwtPlansUrl = entity.getRwtPlansUrl();
        this.rwtPlansCheckDate = entity.getRwtPlansCheckDate();
        this.rwtResultsUrl = entity.getRwtResultsUrl();
        this.rwtResultsCheckDate = entity.getRwtResultsCheckDate();
        this.rwtEligibilityYear = entity.getRwtEligibilityYear();
        this.svapNoticeUrl = entity.getSvapNoticeUrl();
        this.lastModifiedDate = entity.getLastModifiedDate();
    }

    public CertifiedProductDetailsDTO(CertifiedProductDetailsEntitySimple entity) {
        this();
        this.id = entity.getId();
        this.productCode = entity.getProductCode();
        this.versionCode = entity.getVersionCode();
        this.icsCode = entity.getIcsCode();
        this.additionalSoftwareCode = entity.getAdditionalSoftwareCode();
        this.creationDate = entity.getCreationDate();
        this.certifiedDateCode = entity.getCertifiedDateCode();
        this.acbCertificationId = entity.getAcbCertificationId();
        this.certificationBodyId = entity.getCertificationBodyId();
        this.certificationBodyName = entity.getCertificationBodyName();
        this.certificationBodyCode = entity.getCertificationBodyCode();
        this.certificationEditionId = entity.getCertificationEditionId();
        this.certificationStatusId = entity.getCertificationStatusId();
        this.certificationStatusName = entity.getCertificationStatusName();
        this.certificationStatusDate = entity.getCertificationStatusDate();
        this.curesUpdate = entity.getCuresUpdate();
        this.chplProductNumber = entity.getChplProductNumber();
        this.otherAcb = entity.getOtherAcb();
        this.practiceTypeId = entity.getPracticeTypeId();
        this.practiceTypeName = entity.getPracticeTypeName();
        this.productClassificationName = entity.getProductClassificationName();
        this.productClassificationTypeId = entity.getProductClassificationTypeId();
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.sedIntendedUserDescription = entity.getSedIntendedUserDescription();
        this.sedTestingEnd = entity.getSedTestingEnd();

        this.developer = new DeveloperDTO();
        this.developer.setId(entity.getDeveloperId());
        this.developer.setName(entity.getDeveloperName());
        this.developer.setDeveloperCode(entity.getDeveloperCode());
        this.developer.setWebsite(entity.getDeveloperWebsite());
        this.developer.setSelfDeveloper(entity.getSelfDeveloper());

        if (entity.getDeveloperStatusId() != null) {
            developerCurrentStatus = new DeveloperStatusEventDTO();
            developerCurrentStatus.setDeveloperId(entity.getDeveloperId());
            DeveloperStatusDTO statusObj = new DeveloperStatusDTO();
            statusObj.setId(entity.getDeveloperStatusId());
            statusObj.setStatusName(entity.getDeveloperStatusName());
            developerCurrentStatus.setStatus(statusObj);
            developerCurrentStatus.setStatusDate(entity.getDeveloperStatusDate());
            this.developer.getStatusEvents().add(developerCurrentStatus);
        }

        if (entity.getAddressId() != null) {
            AddressDTO developerAddress = new AddressDTO();
            developerAddress.setId(entity.getAddressId());
            developerAddress.setStreetLineOne(entity.getStreetLine1());
            developerAddress.setStreetLineTwo(entity.getStreetLine2());
            developerAddress.setCity(entity.getCity());
            developerAddress.setState(entity.getState());
            developerAddress.setZipcode(entity.getZipcode());
            developerAddress.setCountry(entity.getCountry());
            this.developer.setAddress(developerAddress);
        }
        if (entity.getContactId() != null) {
            ContactDTO developerContact = new ContactDTO();
            developerContact.setId(entity.getContactId());
            developerContact.setFullName(entity.getFullName());
            developerContact.setEmail(entity.getEmail());
            developerContact.setPhoneNumber(entity.getPhoneNumber());
            developerContact.setTitle(entity.getTitle());
            this.developer.setContact(developerContact);
        }

        if (entity.getProductId() != null) {
            this.product = new ProductDTO();
            this.product.setId(entity.getProductId());
            this.product.setName(entity.getProductName());
        }

        if (entity.getProductVersionId() != null) {
            this.version = new ProductVersionDTO();
            this.version.setId(entity.getProductVersionId());
            this.version.setVersion(entity.getProductVersion());
        }

        this.ics = entity.getIcs();
        this.sedTesting = entity.getSedTesting();
        this.qmsTesting = entity.getQmsTesting();
        this.accessibilityCertified = entity.getAccessibilityCertified();
        this.productAdditionalSoftware = entity.getProductAdditionalSoftware();
        this.mandatoryDisclosures = entity.getMandatoryDisclosures();
        this.year = entity.getYear();
        this.certificationDate = entity.getCertificationDate();
        this.decertificationDate = entity.getDecertificationDate();
        this.countCqms = entity.getCountCqms();
        this.countCertifications = entity.getCountCertifications();
        this.countSurveillance = entity.getCountSurveillance();
        this.countOpenSurveillance = entity.getCountOpenSurveillance();
        this.countClosedSurveillance = entity.getCountClosedSurveillance();
        this.countOpenNonconformities = entity.getCountOpenNonconformities();
        this.countClosedNonconformities = entity.getCountClosedNonconformities();
        this.rwtPlansUrl = entity.getRwtPlansUrl();
        this.rwtPlansCheckDate = entity.getRwtPlansCheckDate();
        this.rwtResultsUrl = entity.getRwtResultsUrl();
        this.rwtResultsCheckDate = entity.getRwtResultsCheckDate();
        this.rwtEligibilityYear = entity.getRwtEligibilityYear();
        this.svapNoticeUrl = entity.getSvapNoticeUrl();
        this.lastModifiedDate = entity.getLastModifiedDate();
    }

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getCertificationDate() {
        return Util.getNewDate(certificationDate);
    }

    public void setCertificationDate(Date certificationDate) {
        this.certificationDate = Util.getNewDate(certificationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public Date getSedTestingEnd() {
        return Util.getNewDate(sedTestingEnd);
    }

    public void setSedTestingEnd(Date sedTestingEnd) {
        this.sedTestingEnd = Util.getNewDate(sedTestingEnd);
    }

    public Date getCertificationStatusDate() {
        return Util.getNewDate(certificationStatusDate);
    }

    public void setCertificationStatusDate(Date certificationStatusDate) {
        this.certificationStatusDate = Util.getNewDate(certificationStatusDate);
    }

    public Date getDecertificationDate() {
        return Util.getNewDate(decertificationDate);
    }

    public void setDecertificationDate(Date decertificationDate) {
        this.decertificationDate = Util.getNewDate(decertificationDate);
    }


    /**
     * Return two digit year of Listing.
     *
     * @return two digit year
     */
    public String getYearCode() {
        if (StringUtils.isEmpty(this.getYear())) {
            return "";
        } else if (this.getYear().length() == 2) {
            return this.getYear();
        } else if (this.getYear().length() == FOUR_DIGIT_YEAR) {
            return this.getYear().substring(this.getYear().length() - 2);
        }
        return "??";
    }
}
