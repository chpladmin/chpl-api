package gov.healthit.chpl.datadog;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.model.SyntheticsAPITest;
import com.datadog.api.client.v1.model.SyntheticsAPITestResultFull;
import com.datadog.api.client.v1.model.SyntheticsGetAPITestLatestResultsResponse;
import com.datadog.api.client.v1.model.SyntheticsTriggerBody;
import com.datadog.api.client.v1.model.SyntheticsTriggerTest;

import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import gov.healthit.chpl.scheduler.job.urluptime.DatadogSyntheticsTestResultService;
import gov.healthit.chpl.scheduler.job.urluptime.DatadogSyntheticsTestService;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class OnDemandUrlCheckerManager {
    private static final Long TEMP_DEVELOPER_ID = -99L;
    private static final Integer MAX_ATTEMPTS = 45;
    private static final Integer MAX_SECONDS = 45;
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

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).URL_CHECKER, "
            + "T(gov.healthit.chpl.permissions.domains.UrlCheckerDomainPermissions).CHECK)")
    public OnDemandUrlCheckerResponse checkUrl(String url) throws ApiException {
        SyntheticsAPITest test = null;
        try {
            test = createTest(url);
            triggerTest(test);
            SyntheticsGetAPITestLatestResultsResponse result = awaitTestResults(test);
            OnDemandUrlCheckerResponse response = analyzeTestResults(result, test);
            return response;
        } finally {
            LOGGER.info("Completed On Demand URL Check");
            if (test != null) {
                LOGGER.info("Deleting On Demand URL Check");
                datadogSyntheticsTestService.deleteSyntheticsTests(List.of(test.getPublicId()));
            }
        }
    }

    private SyntheticsAPITest createTest(String url) {
        LOGGER.info("Creating On Demand URL Check");
        return datadogSyntheticsTestService.createSyntheticsTest(url, List.of(TEMP_DEVELOPER_ID));
    }

    private void triggerTest(SyntheticsAPITest test) throws ApiException {
        LOGGER.info("Triggering On Demand URL Check");
        SyntheticsTriggerBody body = new SyntheticsTriggerBody()
                .tests(List.of(new SyntheticsTriggerTest().publicId(test.getPublicId())));

        datadogSyntheticsTestService.getApiProvider().getApiInstance().triggerTests(body);
    }

    private SyntheticsGetAPITestLatestResultsResponse awaitTestResults(SyntheticsAPITest test) throws ApiException {
        LOGGER.info("Awaiting On Demand URL Check");
        RetryPolicy<SyntheticsGetAPITestLatestResultsResponse> retryPolicy = RetryPolicy.<SyntheticsGetAPITestLatestResultsResponse>builder()
                .withMaxAttempts(MAX_ATTEMPTS)
                .withDelay(Duration.ofSeconds(1))
                .withMaxDuration(Duration.ofSeconds(MAX_SECONDS))
                .onRetry(e -> LOGGER.info("Failure #{}. Retrying.", e.getAttemptCount()))
                .onSuccess(e -> LOGGER.info("Success #{}.", e.getAttemptCount()))
                .handleResultIf(res -> res == null || res.getResults().size() == 0)
                .build();

        return Failsafe.with(retryPolicy)
                .get(() -> datadogSyntheticsTestResultService.getSyntheticsTestResults(test.getPublicId()));
    }

    private OnDemandUrlCheckerResponse analyzeTestResults(SyntheticsGetAPITestLatestResultsResponse result, SyntheticsAPITest test) throws ApiException {
        SyntheticsAPITestResultFull fullTestResults = null;
        OnDemandUrlCheckerResponse response = null;
        if (result != null
                && result.getResults().size() > 0) {
            fullTestResults = datadogSyntheticsTestResultService.getDetailedTestResult(test.getPublicId(), result.getResults().get(0).getResultId());
            response = convertToResponse(test.getConfig().getRequest().getUrl(), fullTestResults);
            response.setPassed(result.getResults().get(0).getResult().getPassed());
            if (!result.getResults().get(0).getResult().getPassed()) {
                response.setErrorMessage(fullTestResults.getResult().getFailure().getMessage());
            }
        } else {
            throw new ApiException("No results found for test " + test.getPublicId());
        }
        return response;
    }

    private OnDemandUrlCheckerResponse convertToResponse(String url, SyntheticsAPITestResultFull fullTestResults) {
        if (doAdditionalPropertiesExist(fullTestResults)) {
            return OnDemandUrlCheckerResponse.builder()
                    .httpResponseAssertion(getAssertionResult(fullTestResults.getResult().getAdditionalProperties(), TYPE_VALUE_STATUS_CODE))
                    .responseTimeAssertion(getAssertionResult(fullTestResults.getResult().getAdditionalProperties(), TYPE_VALUE_RESPONSE_TIME))
                    .bodyNotEmptyAssertion(getAssertionResult(fullTestResults.getResult().getAdditionalProperties(), TYPE_VALUE_BODY))
                    .url(url)
                    .build();
        } else {
            return OnDemandUrlCheckerResponse.builder()
                    .url(url)
                    .build();
        }
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
                            .actualValue(((Map<?, ?>) map).containsKey(ACTUAL_KEY) ? ((Map<?, ?>) map).get(ACTUAL_KEY).toString() : "")
                            .build())
                    .orElse(OnDemandUrlCheckerAssertionResult.builder()
                            .passed(false)
                            .actualValue("Unknown")
                            .build());
        }
        return null;
    }

    private Boolean doAdditionalPropertiesExist(SyntheticsAPITestResultFull fullTestResults) {
        return fullTestResults.getResult().getAdditionalProperties().containsKey(ASSERTION_RESULTS_KEY)
                && fullTestResults.getResult().getAdditionalProperties().get(ASSERTION_RESULTS_KEY) instanceof List<?>;
    }
}
