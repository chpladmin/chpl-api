package gov.healthit.chpl.realworldtesting.domain;

import gov.healthit.chpl.api.deprecatedUsage.DeprecatedResponseField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class RealWorldTestingUrlByDeveloper {
    private String url;

    @Deprecated
    @DeprecatedResponseField(message = "This field is unused and will be removed.", removalDate = "2026-09-01")
    private Long activeCertificateCount;
}
