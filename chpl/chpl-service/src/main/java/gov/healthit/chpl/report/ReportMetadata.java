package gov.healthit.chpl.report;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import jakarta.persistence.Transient;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReportMetadata {
    private Long id;
    private String environment;
    private String title;
    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2027-01-01")
    private String reportKey;
    private String url;
    private String height;

    @Transient
    @JsonIgnore
    //this field will be used to filter reports based on the role of the current user
    //but does not need to be included in the API responses
    private List<String> roleNames;
}
