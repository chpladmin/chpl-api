package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.web.controller.InvalidArgumentsException;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;

public class CertifiedProductDTO implements Serializable {
    private static final long serialVersionUID = 7918387302717979598L;
    public static final int CHPL_PRODUCT_ID_PARTS = 9;
    public static final int EDITION_CODE_INDEX = 0;
    public static final int ATL_CODE_INDEX = 1;
    public static final int ACB_CODE_INDEX = 2;
    public static final int DEVELOPER_CODE_INDEX = 3;
    public static final int PRODUCT_CODE_INDEX = 4;
    public static final int VERSION_CODE_INDEX = 5;
    public static final int ICS_CODE_INDEX = 6;
    public static final int ADDITIONAL_SOFTWARE_CODE_INDEX = 7;
    public static final int CERTIFIED_DATE_CODE_INDEX = 8;

    public static final int PRODUCT_CODE_LENGTH = 4;
    public static final int VERSION_CODE_LENGTH = 2;
    public static final int ICS_CODE_LENGTH = 2;
    public static final int ADDITIONAL_SOFTWARE_CODE_LENGTH = 1;
    public static final int CERTIFIED_DATE_CODE_LENGTH = 6;

    private Long id;
    private String productCode;
    private String versionCode;
    private String icsCode;
    private String additionalSoftwareCode;
    private String certifiedDateCode;
    private String acbCertificationId;
    private Long certificationBodyId;
    private Long certificationEditionId;
    private String chplProductNumber;
    private Date creationDate;
    private Boolean deleted;
    private Date lastModifiedDate;
    private Long lastModifiedUser;
    private Long practiceTypeId;
    private Long productClassificationTypeId;
    private Long productVersionId;
    private Long meaningfulUseUsers;
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    private Date sedTestingEnd;
    private Long testingLabId;
    private CertificationStatusDTO certificationStatus;
    private String otherAcb;
    private String transparencyAttestationUrl;
    private Boolean ics;
    private Boolean sedTesting;
    private Boolean qmsTesting;
    private Boolean accessibilityCertified;
    private String productAdditionalSoftware;
    private Boolean transparencyAttestation = null;

    public CertifiedProductDTO() {
    }

    public CertifiedProductDTO(CertifiedProductEntity entity) {
        this.id = entity.getId();
        this.productCode = entity.getProductCode();
        this.versionCode = entity.getVersionCode();
        this.icsCode = entity.getIcsCode();
        this.additionalSoftwareCode = entity.getAdditionalSoftwareCode();
        this.certifiedDateCode = entity.getCertifiedDateCode();
        this.acbCertificationId = entity.getAcbCertificationId();
        this.certificationBodyId = entity.getCertificationBodyId();
        this.certificationEditionId = entity.getCertificationEditionId();
        this.chplProductNumber = entity.getChplProductNumber();
        this.creationDate = entity.getCreationDate();
        this.deleted = entity.getDeleted();
        this.lastModifiedDate = entity.getLastModifiedDate();
        this.lastModifiedUser = entity.getLastModifiedUser();
        this.meaningfulUseUsers = entity.getMeaningfulUseUsers();
        this.practiceTypeId = entity.getPracticeTypeId();
        this.productClassificationTypeId = entity.getProductClassificationTypeId();
        this.productVersionId = entity.getProductVersionId();
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.sedIntendedUserDescription = entity.getSedIntendedUserDescription();
        this.sedTestingEnd = entity.getSedTestingEnd();
        this.transparencyAttestationUrl = entity.getTransparencyAttestationUrl();
        this.testingLabId = entity.getTestingLabId();
        this.otherAcb = entity.getOtherAcb();
        this.setIcs(entity.getIcs());
        this.setSedTesting(entity.getSedTesting());
        this.setQmsTesting(entity.getQmsTesting());
        this.setAccessibilityCertified(entity.getAccessibilityCertified());
        this.setProductAdditionalSoftware(entity.getProductAdditionalSoftware());
    }

    public CertifiedProductDTO(CertifiedProductSearchDetails from) throws InvalidArgumentsException {
        this.setId(from.getId());
        if (from.getTestingLab() != null && !StringUtils.isEmpty(from.getTestingLab().get("id"))) {
            this.setTestingLabId(new Long(from.getTestingLab().get("id").toString()));
        }
        this.setCertificationBodyId(new Long(from.getCertifyingBody().get("id").toString()));
        if (from.getPracticeType() != null && from.getPracticeType().get("id") != null) {
            this.setPracticeTypeId(new Long(from.getPracticeType().get("id").toString()));
        }
        if (from.getClassificationType() != null && from.getClassificationType().get("id") != null) {
            this.setProductClassificationTypeId(new Long(from.getClassificationType().get("id").toString()));
        }
        this.setProductVersionId(new Long(from.getVersion().getVersionId()));
        
        CertificationStatus fromStatus = from.getCurrentStatus().getStatus();
        if(fromStatus != null) {
            this.certificationStatus = new CertificationStatusDTO();
            this.certificationStatus.setId(fromStatus.getId());
            this.certificationStatus.setStatus(fromStatus.getName());
        }
        this.setCertificationEditionId(new Long(from.getCertificationEdition().get("id").toString()));
        this.setReportFileLocation(from.getReportFileLocation());
        this.setSedReportFileLocation(from.getSedReportFileLocation());
        this.setSedIntendedUserDescription(from.getSedIntendedUserDescription());
        this.setSedTestingEnd(from.getSedTestingEndDate());
        this.setAcbCertificationId(from.getAcbCertificationId());
        this.setOtherAcb(from.getOtherAcb());
        this.setIcs(from.getIcs() == null || from.getIcs().getInherits() == null ? false : from.getIcs().getInherits());
        this.setAccessibilityCertified(from.getAccessibilityCertified());
        this.setProductAdditionalSoftware(from.getProductAdditionalSoftware());

        this.setTransparencyAttestationUrl(from.getTransparencyAttestationUrl());

        // set the pieces of the unique id
        if (!StringUtils.isEmpty(from.getChplProductNumber())) {
            if (from.getChplProductNumber().startsWith("CHP-")) {
                this.setChplProductNumber(from.getChplProductNumber());
            } else {
                String chplProductId = from.getChplProductNumber();
                String[] chplProductIdComponents = chplProductId.split("\\.");
                if (chplProductIdComponents == null || chplProductIdComponents.length != 9) {
                    throw new InvalidArgumentsException(
                            "CHPL Product Id " + chplProductId + " is not in a format recognized by the system.");
                } else {
                    this.setProductCode(chplProductIdComponents[4]);
                    this.setVersionCode(chplProductIdComponents[5]);
                    this.setIcsCode(chplProductIdComponents[6]);
                    this.setAdditionalSoftwareCode(chplProductIdComponents[7]);
                    this.setCertifiedDateCode(chplProductIdComponents[8]);
                }

                if (from.getCertificationDate() != null) {
                    Date certDate = new Date(from.getCertificationDate());
                    SimpleDateFormat dateCodeFormat = new SimpleDateFormat("yyMMdd");
                    String dateCode = dateCodeFormat.format(certDate);
                    this.setCertifiedDateCode(dateCode);
                }

                if (from.getCertificationResults() != null && from.getCertificationResults().size() > 0) {
                    boolean hasSoftware = false;
                    for (CertificationResult cert : from.getCertificationResults()) {
                        if (cert.getAdditionalSoftware() != null && cert.getAdditionalSoftware().size() > 0) {
                            hasSoftware = true;
                        }
                    }
                    if (hasSoftware) {
                        this.setAdditionalSoftwareCode("1");
                    } else {
                        this.setAdditionalSoftwareCode("0");
                    }
                }
            }
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getAcbCertificationId() {
        return acbCertificationId;
    }

    public void setAcbCertificationId(final String acbCertificationId) {
        this.acbCertificationId = acbCertificationId;
    }

    public Long getCertificationBodyId() {
        return certificationBodyId;
    }

    public void setCertificationBodyId(final Long certificationBodyId) {
        this.certificationBodyId = certificationBodyId;
    }

    public Long getCertificationEditionId() {
        return certificationEditionId;
    }

    public void setCertificationEditionId(final Long certificationEditionId) {
        this.certificationEditionId = certificationEditionId;
    }

    public String getChplProductNumber() {
        return chplProductNumber;
    }

    public void setChplProductNumber(final String chplProductNumber) {
        this.chplProductNumber = chplProductNumber;
    }

    public Date getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(final Date creationDate) {
        this.creationDate = creationDate;
    }

    public Boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(final Boolean deleted) {
        this.deleted = deleted;
    }

    public Date getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(final Date lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
    }

    public Long getLastModifiedUser() {
        return lastModifiedUser;
    }

    public void setLastModifiedUser(final Long lastModifiedUser) {
        this.lastModifiedUser = lastModifiedUser;
    }

    public Long getMeaningfulUseUsers() {
        return meaningfulUseUsers;
    }

    public void setMeaningfulUseUsers(final Long meaningfulUseUsers) {
        this.meaningfulUseUsers = meaningfulUseUsers;
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

    public Long getProductVersionId() {
        return productVersionId;
    }

    public void setProductVersionId(final Long productVersionId) {
        this.productVersionId = productVersionId;
    }

    public String getReportFileLocation() {
        return reportFileLocation;
    }

    public void setReportFileLocation(final String reportFileLocation) {
        this.reportFileLocation = reportFileLocation;
    }

    public Long getTestingLabId() {
        return testingLabId;
    }

    public void setTestingLabId(final Long testingLabId) {
        this.testingLabId = testingLabId;
    }
    
    public String getOtherAcb() {
        return otherAcb;
    }

    public void setOtherAcb(final String otherAcb) {
        this.otherAcb = otherAcb;
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

    public String getIcsCode() {
        return icsCode;
    }

    public void setIcsCode(final String icsCode) {
        this.icsCode = icsCode;
    }

    public Boolean getTransparencyAttestation() {
        return transparencyAttestation;
    }

    public void setTransparencyAttestation(final Boolean transparencyAttestation) {
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

    public String getSedReportFileLocation() {
        return sedReportFileLocation;
    }

    public void setSedReportFileLocation(final String sedReportFileLocation) {
        this.sedReportFileLocation = sedReportFileLocation;
    }

    public Boolean getQmsTesting() {
        return qmsTesting;
    }

    public void setQmsTesting(final Boolean qmsTesting) {
        this.qmsTesting = qmsTesting;
    }

    public String getProductAdditionalSoftware() {
        return productAdditionalSoftware;
    }

    public void setProductAdditionalSoftware(final String productAdditionalSoftware) {
        this.productAdditionalSoftware = productAdditionalSoftware;
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
        return sedTestingEnd;
    }

    public void setSedTestingEnd(final Date sedTestingEnd) {
        this.sedTestingEnd = sedTestingEnd;
    }

    public CertificationStatusDTO getCertificationStatus() {
        return certificationStatus;
    }

    public void setCertificationStatus(CertificationStatusDTO certificationStatus) {
        this.certificationStatus = certificationStatus;
    }
}
