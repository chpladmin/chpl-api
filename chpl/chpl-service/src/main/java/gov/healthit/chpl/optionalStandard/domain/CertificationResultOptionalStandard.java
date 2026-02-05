package gov.healthit.chpl.optionalStandard.domain;

import java.io.Serializable;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnore;
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

    @Schema(description = "The Optional Standard associated with this certification result.")
    private OptionalStandard optionalStandard;

    @JsonIgnore
    private String userEnteredValue;

    public CertificationResultOptionalStandard(CertificationResultOptionalStandardEntity entity) {
        this.id = entity.getId();
        if (entity.getOptionalStandard() != null) {
            this.optionalStandard = entity.getOptionalStandard().toDomain();
        }
    }

    public boolean matches(CertificationResultOptionalStandard existingItem) {
        return Objects.equals(this.optionalStandard.getId(), existingItem.getOptionalStandard().getId())
                || this.optionalStandard.getDisplayValue().equalsIgnoreCase(existingItem.getOptionalStandard().getDisplayValue());
    }
}
