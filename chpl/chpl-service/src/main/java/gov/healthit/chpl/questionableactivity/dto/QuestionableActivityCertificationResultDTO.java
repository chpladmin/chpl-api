package gov.healthit.chpl.questionableactivity.dto;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.questionableactivity.entity.QuestionableActivityCertificationResultEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class QuestionableActivityCertificationResultDTO extends QuestionableActivity {
    private Long certResultId;
    private String reason;
    private CertificationResultDetailsDTO certResult;
    private CertifiedProductDetailsDTO listing;

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

    @Override
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
