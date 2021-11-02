package gov.healthit.chpl.dto.questionableActivity;

import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.entity.questionableActivity.QuestionableActivityListingEntity;
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
public class QuestionableActivityListingDTO extends QuestionableActivityDTO {
    private Long listingId;
    private String certificationStatusChangeReason;
    private String reason;
    private CertifiedProductDetailsDTO listing;

    public QuestionableActivityListingDTO(QuestionableActivityListingEntity entity) {
        super(entity);
        this.listingId = entity.getListingId();
        this.certificationStatusChangeReason = entity.getCertificationStatusChangeReason();
        this.reason = entity.getReason();
        if (entity.getListing() != null) {
            this.listing = new CertifiedProductDetailsDTO(entity.getListing());
        }
    }

    @Override
    public Class<?> getActivityObjectClass() {
        return CertifiedProductDetailsDTO.class;
    }
}
