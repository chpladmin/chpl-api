package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultOptionalStandardEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingCertificationResultOptionalStandardDTO implements Serializable {
    private static final long serialVersionUID = -4957314047711686694L;
    private Long id;
    private Long pendingCertificationResultId;
    private Long optionalStandardId;
    private String citation;

    public PendingCertificationResultOptionalStandardDTO(PendingCertificationResultOptionalStandardEntity entity) {
        this.setId(entity.getId());
        this.setPendingCertificationResultId(entity.getPendingCertificationResultId());
        this.setOptionalStandardId(entity.getOptionalStandardId());
        this.setCitation(entity.getOptionalStandardCitation());
    }
}
