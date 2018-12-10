package gov.healthit.chpl.dto.questionableActivity;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityCertificationResultEntity;

public class QuestionableActivityCertificationResultDTO extends QuestionableActivityDTO {
    private Long certResultId;
    private String reason;
    private CertificationResultDetailsDTO certResult;
    private CertifiedProductDetailsDTO listing;

    public QuestionableActivityCertificationResultDTO() {
        super();
    }

    public QuestionableActivityCertificationResultDTO(QuestionableActivityCertificationResultEntity entity) {
        super(entity);
        this.certResultId = entity.getCertResultId();
        this.reason = entity.getReason();
        if (entity.getCertResult() != null) {
            this.certResult = new CertificationResultDetailsDTO(entity.getCertResult());

            if (entity.getCertResult().getListing() != null) {
                this.listing = new CertifiedProductDetailsDTO(entity.getCertResult().getListing());
            }
        }
    }

    public Class<?> getActivityObjectClass() {
        return CertificationResultDetailsDTO.class;
    }

    public Long getCertResultId() {
        return certResultId;
    }

    public void setCertResultId(Long certResultId) {
        this.certResultId = certResultId;
    }

    public CertificationResultDetailsDTO getCertResult() {
        return certResult;
    }

    public void setCertResult(CertificationResultDetailsDTO certResult) {
        this.certResult = certResult;
    }

    public CertifiedProductDetailsDTO getListing() {
        return listing;
    }

    public void setListing(CertifiedProductDetailsDTO listing) {
        this.listing = listing;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
