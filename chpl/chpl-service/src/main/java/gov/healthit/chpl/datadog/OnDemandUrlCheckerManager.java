package gov.healthit.chpl.datadog;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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

            // Integer attempts = 0;
            // while ((result == null || result.getResults().size() == 0) &&
            // attempts < 45) {
            // Thread.sleep(1000);
            // attempts++;
            // result =
            // datadogSyntheticsTestResultService.getSyntheticsTestResults(test.getPublicId());
            // }

        } finally {
            if (test != null) {
                datadogSyntheticsTestService.deleteSyntheticsTests(List.of(test.getPublicId()));
            }
        }
    }

    private SyntheticsAPITest createTest(String url) {
        return datadogSyntheticsTestService.createSyntheticsTest(url, List.of(TEMP_DEVELOPER_ID));
    }

    private void triggerTest(SyntheticsAPITest test) throws ApiException {
        SyntheticsTriggerBody body = new SyntheticsTriggerBody()
                .tests(List.of(new SyntheticsTriggerTest().publicId(test.getPublicId())));

        datadogSyntheticsTestService.getApiProvider().getApiInstance().triggerTests(body);
    }

    private SyntheticsGetAPITestLatestResultsResponse awaitTestResults(SyntheticsAPITest test) throws ApiException {
        SyntheticsGetAPITestLatestResultsResponse result;
        CompletableFuture<SyntheticsGetAPITestLatestResultsResponse> future = CompletableFuture.supplyAsync(() -> {
            return datadogSyntheticsTestResultService.getSyntheticsTestResults(test.getPublicId());
        });

        try {
            result = future.get(45, TimeUnit.SECONDS);
        } catch (Exception e) {
            LOGGER.error("Error getting test results: " + e.getMessage());
            throw new ApiException("No results found for test " + test.getPublicId());
        } finally {
            datadogSyntheticsTestService.deleteSyntheticsTests(List.of(test.getPublicId()));
        }
        return result;
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
