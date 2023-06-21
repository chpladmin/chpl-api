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

import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "serviceBasedUrlUptimeCreatorJobLogger")
@Component
public class DatadogSyntheticTestService {
    private static final Integer HTTP_STATUS_OK = 200;
    private static final String HTTP_METHOD_GET = "GET";
    private static final Long SECONDS_IN_A_MINUTE = 60L;

    private DatadogSyntheticTestApiProvider apiProvider;
    private Boolean datadogIsReadOnly;
    private String datadogTestStartTime;
    private String datadogTestEndTime;
    private Long datadogCheckEveryMinutes;
    private Integer datadogTestTimeout;
    private String datadogTestLocation;

    public DatadogSyntheticTestService(DatadogSyntheticTestApiProvider apiProvider,
            @Value("${datadog.syntheticTest.readOnly}") Boolean datadogIsReadOnly,
            @Value("${datadog.syntheticTest.startTime}") String datadogTestStartTime,
            @Value("${datadog.syntheticTest.endTime}") String datadogTestEndTime,
            @Value("${datadog.syntheticTest.checkEveryMinutes}") Long datadogCheckEveryMinutes,
            @Value("${datadog.syntheticTest.timeout}") Integer datadogTestTimeout,
            @Value("${datadog.syntheticTest.location}") String datadogTestLocation) {
        this.apiProvider = apiProvider;
        this.datadogIsReadOnly = datadogIsReadOnly;
        this.datadogTestStartTime = datadogTestStartTime;
        this.datadogTestEndTime = datadogTestEndTime;
        this.datadogCheckEveryMinutes = datadogCheckEveryMinutes;
        this.datadogTestTimeout = datadogTestTimeout;
        this.datadogTestLocation = datadogTestLocation;
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
                                        .target(datadogTestTimeout)
                                        .type(SyntheticsAssertionType.RESPONSE_TIME)),
                                new SyntheticsAssertion(new SyntheticsAssertionTarget()
                                        .operator(SyntheticsAssertionOperator.IS)
                                        .target(HTTP_STATUS_OK)
                                        .type(SyntheticsAssertionType.STATUS_CODE))))
                        .request(new SyntheticsTestRequest()
                                    .url(url)
                                    .method(HTTP_METHOD_GET)))
                .options(new SyntheticsTestOptions()
                        .httpVersion(SyntheticsTestOptionsHTTPVersion.ANY)
                        .minFailureDuration(0L)
                        .minLocationFailed(1L)
                        .scheduling(new SyntheticsTestOptionsScheduling()
                                .timezone(DateUtil.ET_ZONE_ID)
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.MONDAY)
                                        .from(datadogTestStartTime)
                                        .to(datadogTestEndTime))
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.TUESDAY)
                                        .from(datadogTestStartTime)
                                        .to(datadogTestEndTime))
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.WEDNESDAY)
                                        .from(datadogTestStartTime)
                                        .to(datadogTestEndTime))
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.THURSDAY)
                                        .from(datadogTestStartTime)
                                        .to(datadogTestEndTime))
                                .addTimeframesItem(new SyntheticsTestOptionsSchedulingTimeframe()
                                        .day(DatadogDayOfWeek.FRIDAY)
                                        .from(datadogTestStartTime)
                                        .to(datadogTestEndTime)))
                        .tickEvery(convertMinutesToSeconds(datadogCheckEveryMinutes)))
                .locations(Collections.singletonList(datadogTestLocation))
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

    private Long convertMinutesToSeconds(Long minutes) {
        return minutes * SECONDS_IN_A_MINUTE;
    }
}
