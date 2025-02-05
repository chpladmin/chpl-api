package gov.healthit.chpl.datadog;

import java.util.Collections;
import java.util.List;

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

    private DatadogSyntheticsTestService datadogSyntheticsTestService;
    private DatadogSyntheticsTestResultService datadogSyntheticsTestResultService;

    @Autowired
    public OnDemandUrlCheckerManager(DatadogSyntheticsTestService datadogSyntheticsTestService, DatadogSyntheticsTestResultService datadogSyntheticsTestResultService) {
        this.datadogSyntheticsTestService = datadogSyntheticsTestService;
        this.datadogSyntheticsTestResultService = datadogSyntheticsTestResultService;
    }

    public SyntheticsAPITestResultFull checkUrl(String url) throws InterruptedException, ApiException {

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
            LOGGER.info("Attempt: {}", attempts);
            result = datadogSyntheticsTestResultService.getSyntheticsTestResults(test.getPublicId());
            LOGGER.info("Result: {}", result);
        }

        SyntheticsAPITestResultFull fullTestResults = null;
        if (result != null && result.getResults().size() > 0) {
            LOGGER.info("Getting detailed results");
            fullTestResults = datadogSyntheticsTestResultService.getDetailedTestResult(test.getPublicId(), result.getResults().get(0).getResultId());
            LOGGER.info("Detailed results: {}", fullTestResults.getResult().getHttpStatusCode());
            LOGGER.info("Deleting test");
            datadogSyntheticsTestService.deleteSyntheticsTests(List.of(test.getPublicId()));
        } else {
            LOGGER.info("Deleting test");
            datadogSyntheticsTestService.deleteSyntheticsTests(List.of(test.getPublicId()));
            LOGGER.info("No results found");
            throw new ApiException("No results found for test " + test.getPublicId());
        }

        return fullTestResults;
    }

}
