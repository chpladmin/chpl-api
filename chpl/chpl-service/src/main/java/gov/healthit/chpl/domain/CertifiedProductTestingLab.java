package gov.healthit.chpl.domain;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
@Data
@AllArgsConstructor
public class CertifiedProductTestingLab implements Serializable {
    private static final long serialVersionUID = -2078691100124619582L;

    @Schema(description = "Testing Lab to listing mapping internal ID")
    private Long id;


    @Schema(description = "Testing Lab")
    private TestingLab testingLab;


    @Deprecated
    @DeprecatedResponseField(removalDate = "2024-04-01", message = "Replaced by testingLab.id")
    @Schema(description = "Testing Lab internal ID")
    private Long testingLabId;

    @Deprecated
    @DeprecatedResponseField(removalDate = "2024-04-01", message = "Replaced by testingLab.name")
    @Schema(description = "The Testing Lab's public name")
    private String testingLabName;

    @Deprecated
    @DeprecatedResponseField(removalDate = "2024-04-01", message = "Replaced by testingLab.atlCode")
    @Schema(description = "The Testing Lab's Code")
    private String testingLabCode;

    public CertifiedProductTestingLab() {
        super();
    }

    public boolean matches(final CertifiedProductTestingLab other) {
        boolean result = false;

        if (other == null || other.getTestingLab() == null || this.getTestingLab() == null) {
            result = false;
        } else if (this.getTestingLab().getId() != null && other.getTestingLab().getId() != null
                && this.getTestingLab().getId().longValue() == other.getTestingLab().getId().longValue()) {
            result = true;
        } else if (this.getTestingLab().getName() != null && other.getTestingLab().getName() != null
                && this.getTestingLab().getName().equalsIgnoreCase(other.getTestingLab().getName())) {
            result = true;
        }
        return result;
    }
}
