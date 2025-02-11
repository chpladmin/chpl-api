package gov.healthit.chpl.datadog;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OnDemandUrlCheckerResponse {
    private String url;
    private String errorMessage;
    private Boolean passed;
    private OnDemandUrlCheckerAssertionResult responseTimeAssertion;
    private OnDemandUrlCheckerAssertionResult bodyNotEmptyAssertion;
    private OnDemandUrlCheckerAssertionResult httpResponseAssertion;
}
