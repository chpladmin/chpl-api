package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import com.datadog.api.client.v1.api.SyntheticsApi;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

//@Log4j2(topic =  "urlUptimeCreatorJobLogger")
@Log4j2
public class UrlUptimeCreatorJob extends QuartzJob {

    private SyntheticsApi apiInstance = null;

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Autowired
    private ChplUptimeMonitorService chplUptimeMonitorService;

    @Autowired
    private ChplUptimeMonitorTestService chplUptimeMonitorTestService;

    @Autowired
    ServiceBasedUrlGatherer serviceBasedUrlGatherer;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Url Uptime Creator job *********");
        try {
//            ApiClient defaultClient = ApiClient.getDefaultApiClient();
//            defaultClient.configureApiKeys(getDatadogSecrets());
//            apiInstance = new SyntheticsApi(defaultClient);
//
//            chplUptimeMonitorService.synchronizeDatadogMonitorsForReporting(apiInstance);
//            chplUptimeMonitorTestService.retrieveTestResultsForPreviousDay(apiInstance);
            serviceBasedUrlGatherer.getAllServiceBasedUrls()
                    .forEach(url -> {
                        LOGGER.info("Developer: {}  ------  {}", url.getDeveloperId(), url.getUrl());
                        LOGGER.info("Chpl Prod Nbrs: {}", url.getChplProductNumbers().stream().collect(Collectors.joining("; ")));
                    });
        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Url Uptime Creator job *********");
    }


}
