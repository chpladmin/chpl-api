package gov.healthit.chpl.datadog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnDemandUrlCheckerAssertionResult {
    private Boolean passed;
    private String actualValue;
}
