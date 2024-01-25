package gov.healthit.chpl.functionalitytested;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Any optional, alternative, ambulatory (2015 only), or inpatient (2015 only) capabilities within a certification "
     + "criterion to which the Health IT module was tested and certified. For example, within the 2015 certification criteria "
     + "170.315(a), the optional functionality to include a \"reason for order\" field should be denoted as \"(a)(1)(ii)\". You "
     + "can find a list of potential values in the 2014 or 2015 Functionality and Standards Reference Tables.")
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CertificationResultFunctionalityTested implements Serializable {
    private static final long serialVersionUID = -1647645050538126758L;

    @Schema(description = "Functionality tested to certification result mapping internal ID")
    private Long id;

    @Schema(description = "Functionality tested internal ID")
    private FunctionalityTested functionalityTested;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found functionalityTested.id",
            removalDate = "2024-01-01")
    private Long functionalityTestedId;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found functionalityTested.value",
            removalDate = "2024-01-01")
    private String description;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed. This data can be found functionalityTested.regulatoryTextCitation",
            removalDate = "2024-01-01")
    private String name;

    @JsonIgnore
    private Long certificationResultId;

    @JsonIgnore
    private Date creationDate;

    public boolean matches(CertificationResultFunctionalityTested anotherFunc) {
        boolean result = false;
        if (this.getFunctionalityTested().getId() != null && anotherFunc.getFunctionalityTested().getId() != null
                && this.getFunctionalityTested().getId().longValue() == anotherFunc.getFunctionalityTested().getId().longValue()) {
            result = true;
        } else if (!StringUtils.isEmpty(this.getFunctionalityTested().getRegulatoryTextCitation())
                    && !StringUtils.isEmpty(anotherFunc.getFunctionalityTested().getRegulatoryTextCitation())
                && this.getFunctionalityTested().getRegulatoryTextCitation().equalsIgnoreCase(anotherFunc.getFunctionalityTested().getRegulatoryTextCitation())) {
            result = true;
        }
        return result;
    }
}
