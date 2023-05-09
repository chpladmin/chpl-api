package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.datadog.api.client.ApiClient;
import com.datadog.api.client.ApiException;
import com.datadog.api.client.v1.api.SyntheticsApi;
import com.datadog.api.client.v1.api.SyntheticsApi.GetAPITestLatestResultsOptionalParameters;
import com.datadog.api.client.v1.model.SyntheticsAPITestResultShort;
import com.datadog.api.client.v1.model.SyntheticsGetAPITestLatestResultsResponse;
import com.datadog.api.client.v1.model.SyntheticsListTestsResponse;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UrlUptimeCreatorJob extends QuartzJob {

    private SyntheticsApi apiInstance = null;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Value("${datadog.apiKey}")
    private String datadogApiKey;

    @Value("${datadog.appKey}")
    private String datadogAppKey;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Url Uptime Creator job *********");
        try {
            ApiClient defaultClient = ApiClient.getDefaultApiClient();
            defaultClient.configureApiKeys(getDatadogSecrets());
            apiInstance = new SyntheticsApi(defaultClient);

            gatherResultsForAllTests();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Url Uptime Creator job *********");
    }

    private void gatherResultsForAllTests() throws ApiException {
        getAllTests().getTests().stream()
                .peek(result -> LOGGER.info(result.getConfig().getRequest().getUrl()))
                .flatMap(test -> getResultsForTest(test.getPublicId()).stream())
                .peek(result -> LOGGER.info("Test id {} at {} with result {}",
                        result.getResultId(),
                        Instant.ofEpochMilli(result.getCheckTime().longValue()).atZone(ZoneId.of("US/Eastern")).format(formatter),
                        result.getResult().getPassed().toString()))
                .toList();
    }

    private List<SyntheticsAPITestResultShort> getResultsForTest(String publicTestKey) {

        GetAPITestLatestResultsOptionalParameters params = new GetAPITestLatestResultsOptionalParameters();
        ZonedDateTime morning = ZonedDateTime.now(ZoneId.of("US/Eastern")).withHour(4).withMinute(0).minusDays(1);
        ZonedDateTime evening = ZonedDateTime.now(ZoneId.of("US/Eastern")).withHour(16).withMinute(0).minusDays(1);


        params.fromTs(morning.toInstant().toEpochMilli());
        params.toTs(evening.toInstant().toEpochMilli());
        params.probeDc(List.of("azure:eastus"));

        SyntheticsGetAPITestLatestResultsResponse response;
        List<SyntheticsAPITestResultShort> testResults = new ArrayList<SyntheticsAPITestResultShort>();
        try {

            response = apiInstance.getAPITestLatestResults(publicTestKey, params);

            while (response.getResults().size() > 1) {
                testResults.addAll(response.getResults());
                Long ts = getMostRecentTimestamp(response.getResults());
                LOGGER.info(LocalDateTime.ofInstant(Instant.ofEpochMilli(ts), ZoneId.of("US/Eastern")).format(formatter));
                params.fromTs(ts);
                response = apiInstance.getAPITestLatestResults(publicTestKey, params);
            }
        } catch (ApiException e) {
            response = null;
            LOGGER.error("Could not retrieve results for test key: {}", publicTestKey, e);
        }
        return testResults;
    }

    private Long getMostRecentTimestamp(List<SyntheticsAPITestResultShort> testResults) {
        return testResults.stream()
                .mapToLong(result -> Math.round(result.getCheckTime()))
                .max()
                .getAsLong();
    }

    private SyntheticsListTestsResponse getAllTests() throws ApiException {
        return apiInstance.listTests();
    }

    private HashMap<String, String> getDatadogSecrets() {
        HashMap<String, String> secrets = new HashMap<>();
        secrets.put("apiKeyAuth", datadogApiKey);
        secrets.put("appKeyAuth", datadogAppKey);
        return secrets;
    }
}
