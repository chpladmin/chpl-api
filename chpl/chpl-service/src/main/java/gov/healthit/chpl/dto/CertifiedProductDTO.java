package gov.healthit.chpl.dto;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationStatus;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.listing.CertifiedProductEntity;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.Util;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertifiedProductDTO implements Serializable {
    private static final long serialVersionUID = 7918387302717979598L;
    private static final String CERTIFIED_DATE_CODE_FORMAT = "yyMMdd";

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
    private LocalDate sedTestingEnd;
    private CertificationStatus certificationStatus;
    private String otherAcb;
    private String mandatoryDisclosures;
    private Boolean ics;
    private Boolean sedTesting;
    private Boolean qmsTesting;
    private Boolean accessibilityCertified;
    private String productAdditionalSoftware;
    private Long pendingCertifiedProductId;
    private String rwtPlansUrl;
    private LocalDate rwtPlansCheckDate;
    private String rwtResultsUrl;
    private LocalDate rwtResultsCheckDate;
    private Integer rwtEligibilityYear;
    private String svapNoticeUrl;

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
        this.practiceTypeId = entity.getPracticeTypeId();
        this.productClassificationTypeId = entity.getProductClassificationTypeId();
        this.productVersionId = entity.getProductVersionId();
        this.reportFileLocation = entity.getReportFileLocation();
        this.sedReportFileLocation = entity.getSedReportFileLocation();
        this.sedIntendedUserDescription = entity.getSedIntendedUserDescription();
        this.sedTestingEnd = entity.getSedTestingEnd();
        this.mandatoryDisclosures = entity.getMandatoryDisclosures();
        this.otherAcb = entity.getOtherAcb();
        this.setIcs(entity.getIcs());
        this.setSedTesting(entity.getSedTesting());
        this.setQmsTesting(entity.getQmsTesting());
        this.setAccessibilityCertified(entity.getAccessibilityCertified());
        this.setProductAdditionalSoftware(entity.getProductAdditionalSoftware());
        this.setRwtPlansUrl(entity.getRwtPlansUrl());
        this.setRwtPlansCheckDate(entity.getRwtPlansCheckDate());
        this.setRwtResultsUrl(entity.getRwtResultsUrl());
        this.setRwtResultsCheckDate(entity.getRwtResultsCheckDate());
        this.setSvapNoticeUrl(entity.getSvapNoticeUrl());
    }

    public CertifiedProductDTO(CertifiedProductSearchDetails from) throws InvalidArgumentsException {
        this.setId(from.getId());
        this.setCertificationBodyId(
                Long.valueOf(from.getCertifyingBody().get(CertifiedProductSearchDetails.ACB_ID_KEY).toString()));
        if (from.getPracticeType() != null && from.getPracticeType().get("id") != null) {
            this.setPracticeTypeId(Long.valueOf(from.getPracticeType().get("id").toString()));
        }
        if (from.getClassificationType() != null && from.getClassificationType().get("id") != null) {
            this.setProductClassificationTypeId(Long.valueOf(from.getClassificationType().get("id").toString()));
        }
        this.setProductVersionId(from.getVersion().getId());
        this.certificationStatus = from.getCurrentStatus().getStatus();
        this.setCertificationEditionId(from.getEdition().getId());
        this.setReportFileLocation(from.getReportFileLocation());
        this.setSedReportFileLocation(from.getSedReportFileLocation());
        this.setSedIntendedUserDescription(from.getSedIntendedUserDescription());
        this.setSedTestingEnd(from.getSedTestingEndDay());
        this.setAcbCertificationId(from.getAcbCertificationId());
        this.setOtherAcb(from.getOtherAcb());
        this.setIcs(from.getIcs() == null ? null : from.getIcs().getInherits());
        this.setAccessibilityCertified(from.getAccessibilityCertified());
        this.setProductAdditionalSoftware(from.getProductAdditionalSoftware());
        this.setMandatoryDisclosures(from.getMandatoryDisclosures());
        this.setRwtPlansUrl(from.getRwtPlansUrl());
        this.setRwtPlansCheckDate(from.getRwtPlansCheckDate());
        this.setRwtResultsUrl(from.getRwtResultsUrl());
        this.setRwtResultsCheckDate(from.getRwtResultsCheckDate());
        this.setSvapNoticeUrl(from.getSvapNoticeUrl());

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
                }

                if (from.getCertificationDate() != null) {
                    Date certDate = new Date(from.getCertificationDate());
                    SimpleDateFormat dateCodeFormat = new SimpleDateFormat(CERTIFIED_DATE_CODE_FORMAT);
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

    public Date getCreationDate() {
        return Util.getNewDate(creationDate);
    }

    public void setCreationDate(Date creationDate) {
        this.creationDate = Util.getNewDate(creationDate);
    }

    public Date getLastModifiedDate() {
        return Util.getNewDate(lastModifiedDate);
    }

    public void setLastModifiedDate(Date lastModifiedDate) {
        this.lastModifiedDate = Util.getNewDate(lastModifiedDate);
    }

    public LocalDate getSedTestingEnd() {
        return sedTestingEnd;
    }

    public void setSedTestingEnd(LocalDate sedTestingEnd) {
        this.sedTestingEnd = sedTestingEnd;
    }
}
