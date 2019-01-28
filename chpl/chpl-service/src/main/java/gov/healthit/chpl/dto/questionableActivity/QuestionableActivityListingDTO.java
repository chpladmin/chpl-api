package gov.healthit.chpl.dto.questionableActivity;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityListingEntity;

public class QuestionableActivityListingDTO extends QuestionableActivityDTO {
    private Long listingId;
    private String certificationStatusChangeReason;
    private String reason;
    private CertifiedProductDetailsDTO listing;

    public QuestionableActivityListingDTO() {
        super();
    }

    public QuestionableActivityListingDTO(QuestionableActivityListingEntity entity) {
        super(entity);
        this.listingId = entity.getListingId();
        this.certificationStatusChangeReason = entity.getCertificationStatusChangeReason();
        this.reason = entity.getReason();
        if (entity.getListing() != null) {
            this.listing = new CertifiedProductDetailsDTO(entity.getListing());
        }
    }

    public Class<?> getActivityObjectClass() {
        return CertifiedProductDetailsDTO.class;
    }

    public Long getListingId() {
        return listingId;
    }

    public void setListingId(Long listingId) {
        this.listingId = listingId;
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

    public String getCertificationStatusChangeReason() {
        return certificationStatusChangeReason;
    }

    public void setCertificationStatusChangeReason(String certificationStatusChangeReason) {
        this.certificationStatusChangeReason = certificationStatusChangeReason;
    }

}
