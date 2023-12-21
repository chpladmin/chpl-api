package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CertificationEdition implements Serializable {
    private static final long serialVersionUID = 5732322243572571895L;
    @JsonIgnore
    public static final String CURES_SUFFIX = " Cures Update";

    @Deprecated
    @DeprecatedResponseField(message = "Please use the 'id' field", removalDate = "2024-01-01")
    private Long certificationEditionId;

    @Schema(description = "The internal ID of the edition.")
    private Long id;

    @Deprecated
    @DeprecatedResponseField(message = "Please use the 'name' field", removalDate = "2024-01-01")
    private String year;

    @Schema(description = "The name of the edition.")
    private String name;

    @Schema(description = "Whether or not the edition has been retired.")
    private boolean retired;
}
