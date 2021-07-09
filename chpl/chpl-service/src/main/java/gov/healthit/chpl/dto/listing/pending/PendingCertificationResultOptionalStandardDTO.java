package gov.healthit.chpl.dto.listing.pending;

import java.io.Serializable;

import gov.healthit.chpl.entity.listing.pending.PendingCertificationResultOptionalStandardEntity;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
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
    private OptionalStandard optionalStandard;

    public PendingCertificationResultOptionalStandardDTO(PendingCertificationResultOptionalStandardEntity entity) {
        this.id = entity.getId();
        this.citation = entity.getCitation();
        this.optionalStandardId = entity.getOptionalStandardId();
        OptionalStandard os = new OptionalStandard();
        if (entity.getOptionalStandard() != null) {
            os.setId(entity.getOptionalStandard().getId());
            os.setCitation(entity.getOptionalStandard().getCitation());
            os.setDescription(entity.getOptionalStandard().getDescription());
        }
        this.optionalStandard = os;
    }
}
