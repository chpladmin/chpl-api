package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDate;
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
                .map(test -> getResultForTest(test.getPublicId(), yesterday))
                .filter(result -> result != null)
                .peek(result -> LOGGER.info(result.toString()))
                .toList();
    }

    private SyntheticsGetAPITestLatestResultsResponse getResultForTest(String publicTestKey, LocalDate forDate) {

        GetAPITestLatestResultsOptionalParameters x = new GetAPITestLatestResultsOptionalParameters();
        //x.fromTs(weekAgo.toInstant(ZoneOffset.UTC).toEpochMilli());
        //x.toTs(weekAgo.toInstant(ZoneOffset.UTC).toEpochMilli() + 43200000);
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
        LOGGER.info("apiKey = {}", datadogApiKey);
        HashMap<String, String> secrets = new HashMap<>();
        secrets.put("apiKeyAuth", datadogApiKey);
        //secrets.put("appKeyAuth", "");
        return secrets;
    }
}
