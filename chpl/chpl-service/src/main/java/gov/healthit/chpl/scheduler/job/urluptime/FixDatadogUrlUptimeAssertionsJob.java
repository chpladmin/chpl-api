package gov.healthit.chpl.scheduler.job.urluptime;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.model.SyntheticsAPITest;
import com.datadog.api.client.v1.model.SyntheticsAPITestConfig;
import com.datadog.api.client.v1.model.SyntheticsAPITestType;
import com.datadog.api.client.v1.model.SyntheticsAssertion;
import com.datadog.api.client.v1.model.SyntheticsAssertionOperator;
import com.datadog.api.client.v1.model.SyntheticsAssertionTarget;
import com.datadog.api.client.v1.model.SyntheticsAssertionType;
import com.datadog.api.client.v1.model.SyntheticsTestOptions;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsHTTPVersion;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsScheduling;
import com.datadog.api.client.v1.model.SyntheticsTestOptionsSchedulingTimeframe;
import com.datadog.api.client.v1.model.SyntheticsTestRequest;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import gov.healthit.chpl.util.DateUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "fixDatadogUrlUptimeAssertionsJobLogger")
public class FixDatadogUrlUptimeAssertionsJob extends QuartzJob {

    private static final Integer HTTP_STATUS_OK = 200;
    private static final String HTTP_METHOD_GET = "GET";
    private static final Long SECONDS_IN_A_MINUTE = 60L;


    @Autowired
    private DatadogSyntheticsTestService datadogSyntheticsTestService;

    @Value("${datadog.syntheticsTest.startTime}")
    private String datadogTestStartTime;

    @Value("${datadog.syntheticsTest.endTime}")
    private String datadogTestEndTime;

    @Value("${datadog.syntheticsTest.checkEveryMinutes}")
    private Long datadogCheckEveryMinutes;

    @Value("${datadog.syntheticsTest.timeout}")
    private Integer datadogTestTimeout;

    @Value("${datadog.syntheticsTest.location}")
    private String datadogTestLocation;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Fix Datadog Url Uptime Assertions Job *********");

        datadogSyntheticsTestService.getAllSyntheticsTests().forEach(test -> {
            String url = test.getConfig().getRequest().getUrl();

            if (url.equals("https://www.praxisemr.com/applicationaccess/api/help/")) {
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
                                            .type(SyntheticsAssertionType.STATUS_CODE)),
                                    new SyntheticsAssertion(new SyntheticsAssertionTarget()
                                            .operator(SyntheticsAssertionOperator.MATCHES)
                                            .target("/[\\S]/")
                                            .type(SyntheticsAssertionType.BODY))))
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
                    .tags(test.getTags());

                try {
                    datadogSyntheticsTestService.getApiProvider().getApiInstance().updateAPITest(test.getPublicId(), body);
                    LOGGER.info("Test updated: {}", url);
                } catch (ApiException e) {
                    LOGGER.error("Could not update test for URL: {}", url, e);
                }
            } else {
                LOGGER.info("Test NOT updated: {}", url);
            }
        });

        LOGGER.info("********* Completed the Fix Datadog Url Uptime Assertions Job *********");
    }

    private Optional<SyntheticsAssertion> getContentHeaderAssertionExist(List<SyntheticsAssertion> assertions) {
        return assertions.stream()
                .filter(assertion -> assertion.getSyntheticsAssertionTarget().getType().equals(SyntheticsAssertionType.HEADER)
                        && assertion.getSyntheticsAssertionTarget().getProperty().equals("content-length"))
                .findAny();

    }

    private Long convertMinutesToSeconds(Long minutes) {
        return minutes * SECONDS_IN_A_MINUTE;
    }

}
