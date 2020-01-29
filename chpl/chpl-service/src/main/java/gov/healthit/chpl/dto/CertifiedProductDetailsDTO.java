package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.entity.listing.CertifiedProductDetailsEntity;
import gov.healthit.chpl.util.Util;

/**
 * Certified Product Details DTO.
 * 
 * @author alarned
 *
 */
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
    private Date sedTestingEnd;
    private String acbCertificationId;
    private Long practiceTypeId;
    private String practiceTypeName;
    private Long productClassificationTypeId;
    private String otherAcb;
    private Long certificationStatusId;
    private String certificationStatusName;
    private Date certificationStatusDate;
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
    private Date creationDate;
    private Date certificationDate;
    private Date decertificationDate;
    private Integer countCertifications;
    private Integer countCqms;
    private Integer countSurveillance;
    private Integer countOpenSurveillance;
    private Integer countClosedSurveillance;
    private Integer countOpenNonconformities;
    private Integer countClosedNonconformities;
    private Date lastModifiedDate;
    private Boolean ics;
    private Boolean sedTesting;
    private Boolean qmsTesting;
    private Boolean accessibilityCertified;
    private String productAdditionalSoftware;
    private TransparencyAttestationDTO transparencyAttestation;
    private String transparencyAttestationUrl;
    private Long numMeaningfulUse;

    private static final int FOUR_DIGIT_YEAR = 4;

    /**
     * Default constructor.
     */
    public CertifiedProductDetailsDTO() {
    }

    /**
     * Constructed from entity.
     * 
     * @param entity
     *            the entity
     */
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
        this.numMeaningfulUse = entity.getMeaningfulUseUsers();

        this.developer = new DeveloperDTO();
        this.developer.setId(entity.getDeveloperId());
        this.developer.setName(entity.getDeveloperName());
        this.developer.setDeveloperCode(entity.getDeveloperCode());
        this.developer.setWebsite(entity.getDeveloperWebsite());

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
        this.transparencyAttestationUrl = entity.getTransparencyAttestationUrl();
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
        this.lastModifiedDate = entity.getLastModifiedDate();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTestingLabId() {
        return testingLabId;
    }

    public void setTestingLabId(Long testingLabId) {
        this.testingLabId = testingLabId;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public void setReportFileLocation(String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    public String getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(String acbCertificationId) {
        this.acbCertificationId = acbCertificationId;
    }

    public Long getPracticeTypeId() {
        return practiceTypeId;
    }

    public void setPracticeTypeId(Long practiceTypeId) {
        this.practiceTypeId = practiceTypeId;
    }

    public Long getProductClassificationTypeId() {
        return productClassificationTypeId;
    }

    public void setProductClassificationTypeId(Long productClassificationTypeId) {
        this.productClassificationTypeId = productClassificationTypeId;
    }

    public String getOtherAcb() {
        return otherAcb;
    }

    public void setOtherAcb(String otherAcb) {
        this.otherAcb = otherAcb;
    }

    public Long getCertificationStatusId() {
        return certificationStatusId;
    }

    public void setCertificationStatusId(Long certificationStatusId) {
        this.certificationStatusId = certificationStatusId;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public String getCertificationBodyName() {
        return certificationBodyName;
    }

    public void setCertificationBodyName(String certificationBodyName) {
        this.certificationBodyName = certificationBodyName;
    }

    public String getPracticeTypeName() {
        return practiceTypeName;
    }

    public void setPracticeTypeName(String practiceTypeName) {
        this.practiceTypeName = practiceTypeName;
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

    public String getProductClassificationName() {
        return productClassificationName;
    }

    public void setProductClassificationName(String productClassificationName) {
        this.productClassificationName = productClassificationName;
    }

    public Integer getCountCertifications() {
        return countCertifications;
    }

    public void setCountCertifications(Integer countCertifications) {
        this.countCertifications = countCertifications;
    }

    public Integer getCountCqms() {
        return countCqms;
    }

    public void setCountCqms(Integer countCqms) {
        this.countCqms = countCqms;
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public String getCertificationStatusName() {
        return certificationStatusName;
    }

    public void setCertificationStatusName(String certificationStatusName) {
        this.certificationStatusName = certificationStatusName;
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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getAdditionalSoftwareCode() {
        return additionalSoftwareCode;
    }

    public void setAdditionalSoftwareCode(String additionalSoftwareCode) {
        this.additionalSoftwareCode = additionalSoftwareCode;
    }

    public String getCertifiedDateCode() {
        return certifiedDateCode;
    }

    public void setCertifiedDateCode(String certifiedDateCode) {
        this.certifiedDateCode = certifiedDateCode;
    }

    public String getCertificationBodyCode() {
        return certificationBodyCode;
    }

    public void setCertificationBodyCode(String certificationBodyCode) {
        this.certificationBodyCode = certificationBodyCode;
    }

    public String getIcsCode() {
        return icsCode;
    }

    public void setIcsCode(String icsCode) {
        this.icsCode = icsCode;
    }

    public TransparencyAttestationDTO getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(TransparencyAttestationDTO transparencyAttestation) {
        this.transparencyAttestation = transparencyAttestation;
    }

    public String getTestingLabName() {
        return testingLabName;
    }

    public void setTestingLabName(String testingLabName) {
        this.testingLabName = testingLabName;
    }

    public String getTestingLabCode() {
        return testingLabCode;
    }

    public void setTestingLabCode(String testingLabCode) {
        this.testingLabCode = testingLabCode;
    }

    public Boolean getIcs() {
        return ics;
    }

    public void setIcs(Boolean ics) {
        this.ics = ics;
    }

    public Boolean getSedTesting() {
        return sedTesting;
    }

    public void setSedTesting(Boolean sedTesting) {
        this.sedTesting = sedTesting;
    }

    public Boolean getQmsTesting() {
        return qmsTesting;
    }

    public void setQmsTesting(Boolean qmsTesting) {
        this.qmsTesting = qmsTesting;
    }

    public String getSedReportFileLocation() {
        return sedReportFileLocation;
    }

    public void setSedReportFileLocation(String sedReportFileLocation) {
        this.sedReportFileLocation = sedReportFileLocation;
    }

    public String getProductAdditionalSoftware() {
        return productAdditionalSoftware;
    }

    public void setProductAdditionalSoftware(String productAdditionalSoftware) {
        this.productAdditionalSoftware = productAdditionalSoftware;
    }

    public String getTransparencyAttestationUrl() {
        return transparencyAttestationUrl;
    }

    public void setTransparencyAttestationUrl(String transparencyAttestationUrl) {
        this.transparencyAttestationUrl = transparencyAttestationUrl;
    }

    public Boolean getAccessibilityCertified() {
        return accessibilityCertified;
    }

    public void setAccessibilityCertified(Boolean accessibilityCertified) {
        this.accessibilityCertified = accessibilityCertified;
    }

    public String getSedIntendedUserDescription() {
        return sedIntendedUserDescription;
    }

    public void setSedIntendedUserDescription(String sedIntendedUserDescription) {
        this.sedIntendedUserDescription = sedIntendedUserDescription;
    }

    public Date getSedTestingEnd() {
        return Util.getNewDate(sedTestingEnd);
    }

    public void setSedTestingEnd(Date sedTestingEnd) {
        this.sedTestingEnd = Util.getNewDate(sedTestingEnd);
    }

    public DeveloperDTO getDeveloper() {
        return developer;
    }

    public void setDeveloper(DeveloperDTO developer) {
        this.developer = developer;
    }

    public ProductDTO getProduct() {
        return product;
    }

    public void setProduct(ProductDTO product) {
        this.product = product;
    }

    public ProductVersionDTO getVersion() {
        return version;
    }

    public void setVersion(ProductVersionDTO version) {
        this.version = version;
    }

    public Long getNumMeaningfulUse() {
        return numMeaningfulUse;
    }

    public Integer getCountSurveillance() {
        return countSurveillance;
    }

    public void setCountSurveillance(Integer countSurveillance) {
        this.countSurveillance = countSurveillance;
    }

    public Integer getCountOpenSurveillance() {
        return countOpenSurveillance;
    }

    public void setCountOpenSurveillance(Integer countOpenSurveillance) {
        this.countOpenSurveillance = countOpenSurveillance;
    }

    public Integer getCountClosedSurveillance() {
        return countClosedSurveillance;
    }

    public void setCountClosedSurveillance(Integer countClosedSurveillance) {
        this.countClosedSurveillance = countClosedSurveillance;
    }

    public Integer getCountOpenNonconformities() {
        return countOpenNonconformities;
    }

    public void setCountOpenNonconformities(Integer countOpenNonconformities) {
        this.countOpenNonconformities = countOpenNonconformities;
    }

    public Integer getCountClosedNonconformities() {
        return countClosedNonconformities;
    }

    public void setCountClosedNonconformities(Integer countClosedNonconformities) {
        this.countClosedNonconformities = countClosedNonconformities;
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

    public DeveloperStatusEventDTO getDeveloperCurrentStatus() {
        return developerCurrentStatus;
    }

    public void setDeveloperCurrentStatus(DeveloperStatusEventDTO developerCurrentStatus) {
        this.developerCurrentStatus = developerCurrentStatus;
    }
}
