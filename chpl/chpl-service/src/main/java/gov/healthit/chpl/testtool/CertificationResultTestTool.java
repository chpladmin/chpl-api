package gov.healthit.chpl.testtool;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "The test tool used to certify the Health IT Module to the corresponding "
        + "certification criteria Allowable values are based on the NIST 2014 and 2015 "
        + "Edition Test Tools.")
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class CertificationResultTestTool implements Serializable {
    private static final long serialVersionUID = 2785949879671019720L;

    @Schema(description = "Test tool to certification result mapping internal ID")
    private Long id;

    @Schema(description = "The test tool used to certify the Health IT Module to the corresponding certification criteria")
    private TestTool testTool;

    @Schema(description = "The version of the test tool being used. "
            + "This variable is a string variable that does not take any restrictions on formatting or values.")
    private String version;


    @JsonIgnore
    private Long certificationResultId;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found testTool.testTool.id",
            removalDate = "2024-01-01")
    private Long testToolId;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found testTool.testTool.value",
            removalDate = "2024-01-01")
    private String testToolName;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found testTool.version",
            removalDate = "2024-01-01")
    private String testToolVersion;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found testTool.testTool.retired",
            removalDate = "2024-01-01")
    @Schema(description = "Whether or not the test tool has been retired.")
    private Boolean retired;

    public boolean matches(final CertificationResultTestTool anotherTool) {
        boolean result = false;
        if (this.getTestTool().getId() != null && anotherTool.getTestTool().getId() != null
                && this.getTestTool().getId().longValue() == anotherTool.getTestTool().getId().longValue()
                && ((StringUtils.isEmpty(this.getVersion())
                        && StringUtils.isEmpty(anotherTool.getVersion()))
                     ||
                     (!StringUtils.isEmpty(this.getVersion())
                             && !StringUtils.isEmpty(anotherTool.getVersion())
                             && this.getVersion().equalsIgnoreCase(anotherTool.getVersion())))) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getTestTool().getValue()) && !StringUtils.isEmpty(anotherTool.getTestTool().getValue())
                && this.getTestTool().getValue().equalsIgnoreCase(anotherTool.getTestTool().getValue())
                && ((StringUtils.isEmpty(this.getVersion()) && StringUtils.isEmpty(anotherTool.getVersion()))
                     ||
                     (!StringUtils.isEmpty(this.getVersion())
                             && !StringUtils.isEmpty(anotherTool.getVersion())
                             && this.getVersion().equalsIgnoreCase(anotherTool.getVersion())))) {
            result = true;
        }
        return result;
    }
}
