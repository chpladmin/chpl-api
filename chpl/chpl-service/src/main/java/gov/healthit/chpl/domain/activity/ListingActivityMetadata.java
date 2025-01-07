package gov.healthit.chpl.domain.activity;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class ListingActivityMetadata extends ActivityMetadata {
    private static final long serialVersionUID = 5473773376581297578L;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private String chplProductNumber;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private String acbName;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private Long acbId;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private String developerName;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private String productName;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private String edition;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private Boolean curesUpdate;

    @Deprecated
    @DeprecatedResponseField(message = "This field is deprecated and will be removed.", removalDate = "2025-06-01")
    private Long certificationDate;
}
