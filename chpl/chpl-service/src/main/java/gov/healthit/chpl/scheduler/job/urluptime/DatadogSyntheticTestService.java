package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.model.SyntheticsAPITest;
import com.datadog.api.client.v1.model.SyntheticsAPITestConfig;
import com.datadog.api.client.v1.model.SyntheticsAPITestType;
import com.datadog.api.client.v1.model.SyntheticsAssertion;
import com.datadog.api.client.v1.model.SyntheticsAssertionOperator;
import com.datadog.api.client.v1.model.SyntheticsAssertionTarget;
import com.datadog.api.client.v1.model.SyntheticsAssertionType;
import com.datadog.api.client.v1.model.SyntheticsDeleteTestsPayload;
import com.datadog.api.client.v1.model.SyntheticsTestDetails;
import com.datadog.api.client.v1.model.SyntheticsTestOptions;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsHTTPVersion;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsScheduling;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsSchedulingTimeframe;
import com.datadog.api.client.v1.model.SyntheticsTestRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBasedUrlUptimeCreatorJobLogger")
@Component
public class DatadogSyntheticTestService {

    private DatadogSyntheticTestApiProvider apiProvider;
    private Boolean datadogIsReadOnly;

    public DatadogSyntheticTestService(DatadogSyntheticTestApiProvider apiProvider, @Value("${datadog.syntheticTest.readOnly}") Boolean datadogIsReadOnly) {
        this.apiProvider = apiProvider;
        this.datadogIsReadOnly = datadogIsReadOnly;
    }

    public List<SyntheticsTestDetails> getAllSyntheticTests() {
        try {
            return apiProvider.getApiInstance().listTests().getTests();
        } catch (ApiException e) {
            LOGGER.error("Could not retrieve Synthetic Tests from Datadog", e);
            return null;
        }
    }

    public void deleteSyntheticApiTest(List<String> publicIds) {
        try {
            if (datadogIsReadOnly) {
                LOGGER.info("Not deleting from datadog (due to environment setting) with Public Ids: {}", publicIds);
            } else {
                apiProvider.getApiInstance().deleteTests(new SyntheticsDeleteTestsPayload().publicIds(publicIds));
            }

        } catch (ApiException e) {
            LOGGER.error("Could not delete Synthetic Tests from Datadog: {}", publicIds, e);
        }
    }

    public SyntheticsAPITest createSyntheticApiTest(String url, Long developerId) {
        SyntheticsAPITest body = new SyntheticsAPITest()
                .config(new SyntheticsAPITestConfig()
                        .assertions(Arrays.asList(
                                new SyntheticsAssertion(new SyntheticsAssertionTarget()
                                        .operator(SyntheticsAssertionOperator.LESS_THAN)
                                        .target(5000)
                                        .type(SyntheticsAssertionType.RESPONSE_TIME)),
                                new SyntheticsAssertion(new SyntheticsAssertionTarget()
                                        .operator(SyntheticsAssertionOperator.IS)
                                        .target(200)
                                        .type(SyntheticsAssertionType.STATUS_CODE))))
                        .request(new SyntheticsTestRequest()
                                    .url(url)
                                    .method("GET")))
                .options(new SyntheticsTestOptions()
                        .httpVersion(SyntheticsTestOptionsHTTPVersion.ANY)
                        .minFailureDuration(0L)
                        .minLocationFailed(1L)
                        .scheduling(new SyntheticsTestOptionsScheduling()
                                .timezone("America/New_York")
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.MONDAY)
                                        .from("08:00")
                                        .to("17:00"))
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.TUESDAY)
                                        .from("08:00")
                                        .to("17:00"))
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.WEDNESDAY)
                                        .from("08:00")
                                        .to("17:00"))
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.THURSDAY)
                                        .from("08:00")
                                        .to("17:00"))
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.FRIDAY)
                                        .from("08:00")
                                        .to("17:00")))
                        .tickEvery(1200L))
                .locations(Collections.singletonList("aws:us-east-2"))
                .message("Failed: " + url)
                .type(SyntheticsAPITestType.API)
                .name(url)
                .tags(List.of("developer:" + developerId));

        try {
            if (datadogIsReadOnly) {
                LOGGER.info("Not updating datadog (due to environment setting) with Developer: {} and URL: {}", developerId, url);
                return body;
            } else {
                return apiProvider.getApiInstance().createSyntheticsAPITest(body);
            }
        } catch (ApiException e) {
            LOGGER.error("Could not create Synthetic Tests for URL: {}", url, e);
            return null;
        }
    }
}
