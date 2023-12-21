package gov.healthit.chpl.optionalStandard.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.entity.listing.CertificationResultOptionalStandardEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "An optional standard used to meet a certification criterion. You can find a list of "
     + "potential values in the 2015 Functionality and Standards Reference Tables.")
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CertificationResultOptionalStandard implements Serializable {
    private static final long serialVersionUID = -9182555768595891414L;

    @Schema(description = "Optional standard to certification result mapping internal ID.")
    private Long id;

    @Schema(description = "The Optional Standard internal identifier.")
    private Long optionalStandardId;

    @Schema(description = "The citation for the Optional Standard used to test the associated criteria.")
    private String citation;

    @Schema(description = "The description of the Optional Standard used to test the associated criteria.")
    private String description;

    public CertificationResultOptionalStandard(CertificationResultOptionalStandardEntity entity) {
        this.id = entity.getId();
        if (entity.getOptionalStandard() != null) {
            this.optionalStandardId = entity.getOptionalStandard().getId();
            this.citation = entity.getOptionalStandard().getCitation();
            this.description = entity.getOptionalStandard().getDescription();
        }
    }

    public boolean matches(CertificationResultOptionalStandard existingItem) {
        return this.optionalStandardId.longValue() == existingItem.getOptionalStandardId().longValue()
                || this.citation.equalsIgnoreCase(existingItem.getCitation());
    }
}
