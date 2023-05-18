package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.datadog.api.client.ApiClient;
import com.datadog.api.client.v1.api.SyntheticsApi;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic =  "urlUptimeCreatorJobLogger")
public class UrlUptimeCreatorJob extends QuartzJob {

    private SyntheticsApi apiInstance = null;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    private ChplUptimeMonitorService chplUptimeMonitorService;

    @Autowired
    private ChplUptimeMonitorTestService chplUptimeMonitorTestService;

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

            chplUptimeMonitorService.synchronizeDatadogMonitorsForReporting(apiInstance);
            chplUptimeMonitorTestService.retrieveTestResultsForPreviousDay(apiInstance);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Url Uptime Creator job *********");
    }

    private HashMap<String, String> getDatadogSecrets() {
        HashMap<String, String> secrets = new HashMap<>();
        secrets.put("apiKeyAuth", datadogApiKey);
        secrets.put("appKeyAuth", datadogAppKey);
        return secrets;
    }
}
