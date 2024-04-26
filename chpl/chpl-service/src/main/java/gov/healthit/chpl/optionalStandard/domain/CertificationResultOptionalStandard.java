package gov.healthit.chpl.optionalStandard.domain;

import java.io.Serializable;

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

    //TODO: PUT THESE BACK
//    @Deprecated
//    @DeprecatedResponseField(message = "This field is deprecated and will be removed. Please use optionalStandard.id",
//        removalDate = "2024-10-31")
//    @Schema(description = "The Optional Standard internal identifier.")
//    private Long optionalStandardId;
//
//    @Deprecated
//    @DeprecatedResponseField(message = "This field is deprecated and will be removed. Please use optionalStandard.citation",
//        removalDate = "2024-10-31")
//    @Schema(description = "The citation for the Optional Standard used to test the associated criteria.")
//    private String citation;
//
//    @Deprecated
//    @DeprecatedResponseField(message = "This field is deprecated and will be removed. Please use optionalStandard.description",
//        removalDate = "2024-10-31")
//    @Schema(description = "The description of the Optional Standard used to test the associated criteria.")
//    private String description;

    public CertificationResultOptionalStandard(CertificationResultOptionalStandardEntity entity) {
        this.id = entity.getId();
        if (entity.getOptionalStandard() != null) {
            this.optionalStandard = entity.getOptionalStandard().toDomain();
//            this.optionalStandardId = entity.getOptionalStandard().getId();
//            this.citation = entity.getOptionalStandard().getCitation();
//            this.description = entity.getOptionalStandard().getDescription();
        }
    }

    public boolean matches(CertificationResultOptionalStandard existingItem) {
        return this.optionalStandard.getId().longValue() == existingItem.getOptionalStandard().getId().longValue()
                || this.optionalStandard.getCitation().equalsIgnoreCase(existingItem.getOptionalStandard().getCitation());
    }
}
