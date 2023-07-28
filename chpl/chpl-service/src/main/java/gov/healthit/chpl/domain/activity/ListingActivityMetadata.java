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
    private String chplProductNumber;
    private String acbName;
    private Long acbId;
    private String developerName;
    private String productName;
    @Deprecated
    @DeprecatedResponseField(message = "The certification edition will be removed.", removalDate = "2024-02-01")
    private String edition;
    private Boolean curesUpdate;
    private Long certificationDate;
}
