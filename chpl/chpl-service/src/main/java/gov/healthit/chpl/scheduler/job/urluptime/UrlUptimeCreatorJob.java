package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
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
import com.datadog.api.client.v1.model.SyntheticsGetAPITestLatestResultsResponse;
import com.datadog.api.client.v1.model.SyntheticsListTestsResponse;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class UrlUptimeCreatorJob extends QuartzJob {

    private SyntheticsApi apiInstance = null;

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
        LocalDate yesterday = LocalDate.now().minusDays(1);

        getAllTests().getTests().stream()
                .peek(result -> LOGGER.info(result.getConfig().getRequest().getUrl()))
                .map(test -> getResultForTest(test.getPublicId(), yesterday))
                .filter(result -> result != null)
                .flatMap(result -> result.getResults().stream())
                .peek(result -> LOGGER.info("Test id {} at {} with result {}",
                        result.getResultId(),
                        Instant.ofEpochMilli(result.getCheckTime().longValue()).atZone(ZoneId.systemDefault()).toLocalDateTime(),
                        result.getResult().getPassed().toString()))
                .toList();
    }

    private SyntheticsGetAPITestLatestResultsResponse getResultForTest(String publicTestKey, LocalDate forDate) {

        GetAPITestLatestResultsOptionalParameters x = new GetAPITestLatestResultsOptionalParameters();

        LocalDateTime morning = LocalDateTime.now().withHour(8).withMinute(0).minusDays(1L);
        LocalDateTime evening = LocalDateTime.now().withHour(20).withMinute(0).minusDays(-1);


        x.fromTs(morning.toInstant(ZoneOffset.UTC).toEpochMilli());
        x.toTs(evening.toInstant(ZoneOffset.UTC).toEpochMilli());
        x.probeDc(List.of("azure:eastus"));

        SyntheticsGetAPITestLatestResultsResponse response;
        try {
            response = apiInstance.getAPITestLatestResults(publicTestKey, x);
        } catch (ApiException e) {
            response = null;
            LOGGER.error("Could not retrieve results for test key: {}", publicTestKey, e);
        }
        return response;
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
