package gov.healthit.chpl.datadog;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.model.SyntheticsAPITest;
import com.datadog.api.client.v1.model.SyntheticsAPITestResultFull;
import com.datadog.api.client.v1.model.SyntheticsGetAPITestLatestResultsResponse;
import com.datadog.api.client.v1.model.SyntheticsTriggerBody;
import com.datadog.api.client.v1.model.SyntheticsTriggerTest;

import gov.healthit.chpl.scheduler.job.urluptime.DatadogSyntheticsTestResultService;
import gov.healthit.chpl.scheduler.job.urluptime.DatadogSyntheticsTestService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class OnDemandUrlCheckerManager {
    private static final Long TEMP_DEVELOPER_ID = -99L;
    private static final String ASSERTION_RESULTS_KEY = "assertionResults";
    private static final String TYPE_KEY = "type";
    private static final String ACTUAL_KEY = "actual";
    private static final String VALID_KEY = "valid";
    private static final String TYPE_VALUE_STATUS_CODE = "statusCode";
    private static final String TYPE_VALUE_BODY = "body";
    private static final String TYPE_VALUE_RESPONSE_TIME = "responseTime";

    private DatadogSyntheticsTestService datadogSyntheticsTestService;
    private DatadogSyntheticsTestResultService datadogSyntheticsTestResultService;

    @Autowired
    public OnDemandUrlCheckerManager(DatadogSyntheticsTestService datadogSyntheticsTestService, DatadogSyntheticsTestResultService datadogSyntheticsTestResultService) {
        this.datadogSyntheticsTestService = datadogSyntheticsTestService;
        this.datadogSyntheticsTestResultService = datadogSyntheticsTestResultService;
    }

    public OnDemandUrlCheckerResponse checkUrl(String url) throws InterruptedException, ApiException {

        SyntheticsAPITest test = datadogSyntheticsTestService.createSyntheticsTest(url, List.of(TEMP_DEVELOPER_ID));

        LOGGER.info(test.getPublicId());

        SyntheticsTriggerBody body = new SyntheticsTriggerBody()
                .tests(
                        Collections.singletonList(
                                new SyntheticsTriggerTest().publicId(test.getPublicId())));

        datadogSyntheticsTestService.getApiProvider().getApiInstance().triggerTests(body);

        SyntheticsGetAPITestLatestResultsResponse result = null;
        Integer attempts = 0;
        while ((result == null || result.getResults().size() == 0) && attempts < 45) {
            Thread.sleep(1000);
            attempts++;
            result = datadogSyntheticsTestResultService.getSyntheticsTestResults(test.getPublicId());
        }

        SyntheticsAPITestResultFull fullTestResults = null;
        if (result != null && result.getResults().size() > 0) {
            fullTestResults = datadogSyntheticsTestResultService.getDetailedTestResult(test.getPublicId(), result.getResults().get(0).getResultId());
            datadogSyntheticsTestService.deleteSyntheticsTests(List.of(test.getPublicId()));
        } else {
            datadogSyntheticsTestService.deleteSyntheticsTests(List.of(test.getPublicId()));
            throw new ApiException("No results found for test " + test.getPublicId());
        }
        LOGGER.info("Results: " + fullTestResults.toString());
        return convertToResponse(url, fullTestResults);
    }

    private OnDemandUrlCheckerResponse convertToResponse(String url, SyntheticsAPITestResultFull fullTestResults) {
        return OnDemandUrlCheckerResponse.builder()
                .httpResponseAssertion(getAssertionResult(fullTestResults.getResult().getAdditionalProperties(), TYPE_VALUE_STATUS_CODE))
                .responseTimeAssertion(getAssertionResult(fullTestResults.getResult().getAdditionalProperties(), TYPE_VALUE_RESPONSE_TIME))
                .bodyNotEmptyAssertion(getAssertionResult(fullTestResults.getResult().getAdditionalProperties(), TYPE_VALUE_BODY))
                .url(url)
                .build();
    }

    private OnDemandUrlCheckerAssertionResult getAssertionResult(Map<String, Object> results, String value) {
        if (results.containsKey(ASSERTION_RESULTS_KEY)
                && results.get(ASSERTION_RESULTS_KEY) instanceof List<?>) {

            return ((List<?>) results.get(ASSERTION_RESULTS_KEY)).stream()
                    .filter(map -> map instanceof Map<?, ?>)
                    .filter(map -> ((Map<?, ?>) map).containsKey(TYPE_KEY)
                            && ((Map<?, ?>) map).containsKey(VALID_KEY)
                            && ((String) ((Map<?, ?>) map).get(TYPE_KEY)).equals(value))

                    .findAny()
                    .map(map -> OnDemandUrlCheckerAssertionResult.builder()
                            .passed((Boolean) ((Map<?, ?>) map).get(VALID_KEY))
                            .actualValue(((Map<?, ?>) map).get(ACTUAL_KEY).toString())
                            .build())
                    .orElse(OnDemandUrlCheckerAssertionResult.builder()
                            .passed(false)
                            .actualValue("Unknown")
                            .build());
        }
        return null;
    }
}
