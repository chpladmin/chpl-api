package gov.healthit.chpl.domain.activity;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ProductActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 9069117187924463180L;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private String developerName;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private String productName;

}
