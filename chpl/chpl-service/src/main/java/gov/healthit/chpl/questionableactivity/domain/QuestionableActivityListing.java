package gov.healthit.chpl.questionableactivity.domain;

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
public class QuestionableActivityListing extends QuestionableActivity {
    private Long listingId;
    private String certificationStatusChangeReason;
    private String reason;
    private CertifiedProductDetailsDTO listing;

    @Override
    public Class<?> getActivityObjectClass() {
        return CertifiedProductDetailsDTO.class;
    }
}
