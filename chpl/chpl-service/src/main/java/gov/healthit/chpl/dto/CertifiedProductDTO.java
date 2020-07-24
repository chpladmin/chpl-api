package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import lombok.Data;

@Data
public class CertifiedProductDTO implements Serializable {
    private static final long serialVersionUID = 7918387302717979598L;
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
    private String reportFileLocation;
    private String sedReportFileLocation;
    private String sedIntendedUserDescription;
    private Date sedTestingEnd;
    private CertificationStatusDTO certificationStatus;
    private String otherAcb;
    private String transparencyAttestationUrl;
    private Boolean ics;
    private Boolean sedTesting;
    private Boolean qmsTesting;
    private Boolean accessibilityCertified;
    private String productAdditionalSoftware;
    private Long pendingCertifiedProductId;
    private Boolean transparencyAttestation = null;
    private Integer rwtEligiblityYear;

    public CertifiedProductDTO() {
    }

    public CertifiedProductDTO(final CertifiedProductEntity entity) {
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
        this.practiceTypeId = entity.getPracticeTypeId();
        this.productClassificationTypeId = entity.getProductClassificationTypeId();
        this.productVersionId = entity.getProductVersionId();
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.sedIntendedUserDescription = entity.getSedIntendedUserDescription();
        this.sedTestingEnd = entity.getSedTestingEnd();
        this.transparencyAttestationUrl = entity.getTransparencyAttestationUrl();
        this.otherAcb = entity.getOtherAcb();
        this.setIcs(entity.getIcs());
        this.setSedTesting(entity.getSedTesting());
        this.setQmsTesting(entity.getQmsTesting());
        this.setAccessibilityCertified(entity.getAccessibilityCertified());
        this.setProductAdditionalSoftware(entity.getProductAdditionalSoftware());
        this.setRwtEligiblityYear(entity.getRwtEligibilityYear());
    }

    public CertifiedProductDTO(final CertifiedProductSearchDetails from) throws InvalidArgumentsException {
        this.setId(from.getId());
        this.setCertificationBodyId(
                Long.valueOf(from.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()));
        if (from.getPracticeType() != null && from.getPracticeType().get("id") != null) {
            this.setPracticeTypeId(Long.valueOf(from.getPracticeType().get("id").toString()));
        }
        if (from.getClassificationType() != null && from.getClassificationType().get("id") != null) {
            this.setProductClassificationTypeId(Long.valueOf(from.getClassificationType().get("id").toString()));
        }
        this.setProductVersionId(from.getVersion().getVersionId());

        CertificationStatus fromStatus = from.getCurrentStatus().getStatus();
        if (fromStatus != null) {
            this.certificationStatus = new CertificationStatusDTO();
            this.certificationStatus.setId(fromStatus.getId());
            this.certificationStatus.setStatus(fromStatus.getName());
        }
        this.setCertificationEditionId(
                Long.valueOf(from.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_ID_KEY).toString()));
        this.setReportFileLocation(from.getReportFileLocation());
        this.setSedReportFileLocation(from.getSedReportFileLocation());
        this.setSedIntendedUserDescription(from.getSedIntendedUserDescription());
        this.setSedTestingEnd(from.getSedTestingEndDate());
        this.setAcbCertificationId(from.getAcbCertificationId());
        this.setOtherAcb(from.getOtherAcb());
        this.setIcs(from.getIcs() == null || from.getIcs().getInherits() == null ? Boolean.FALSE : from.getIcs().getInherits());
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
                if (chplProductIdComponents == null
                        || chplProductIdComponents.length != ChplProductNumberUtil.CHPL_PRODUCT_ID_PARTS) {
                    throw new InvalidArgumentsException(
                            "CHPL Product Id " + chplProductId + " is not in a format recognized by the system.");
                } else {
                    this.setProductCode(chplProductIdComponents[ChplProductNumberUtil.PRODUCT_CODE_INDEX]);
                    this.setVersionCode(chplProductIdComponents[ChplProductNumberUtil.VERSION_CODE_INDEX]);
                    this.setIcsCode(chplProductIdComponents[ChplProductNumberUtil.ICS_CODE_INDEX]);
                    this.setAdditionalSoftwareCode(chplProductIdComponents[ChplProductNumberUtil.ADDITIONAL_SOFTWARE_CODE_INDEX]);
                    this.setCertifiedDateCode(chplProductIdComponents[ChplProductNumberUtil.CERTIFIED_DATE_CODE_INDEX]);
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
}
