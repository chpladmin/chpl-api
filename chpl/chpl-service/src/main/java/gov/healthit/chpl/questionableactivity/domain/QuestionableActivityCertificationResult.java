package gov.healthit.chpl.questionableactivity.domain;

import gov.healthit.chpl.dto.CertificationResultDetailsDTO;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
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
public class QuestionableActivityCertificationResult extends QuestionableActivityBase {
    private Long certResultId;
    private String reason;
    private CertificationResultDetailsDTO certResult;
    private CertifiedProductDetailsDTO listing;

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
