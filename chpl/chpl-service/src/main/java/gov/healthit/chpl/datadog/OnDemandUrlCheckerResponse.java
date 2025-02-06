package gov.healthit.chpl.datadog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnDemandUrlCheckerResponse {
    private String url;
    private OnDemandUrlCheckerAssertionResult responseTimeAssertion;
    private OnDemandUrlCheckerAssertionResult bodyNotEmptyAssertion;
    private OnDemandUrlCheckerAssertionResult httpResponseAssertion;
}
