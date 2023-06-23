package gov.healthit.chpl.scheduler.job.urluptime;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic =  "serviceBaseUrlListUptimeCreatorJobLogger")
public class ServiceBaseUrlListUptimeCreatorJob extends QuartzJob {

    @Autowired
    private DatadogUrlUptimeSynchonizer datadogChplSynchonizer;

    @Value("${datadog.syntheticsTest.readOnly}")
    private Boolean datadogReadOnly;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Service Base Url List Uptime Creator job *********");
        if (!datadogReadOnly) {

            try {
                datadogChplSynchonizer.synchronize();
            } catch (Exception e) {
                LOGGER.error(e);
            }
        } else {
            LOGGER.info("Not synchonizing or gathering Service Base URL List data based on configuration");
        }
        LOGGER.info("********* Completed the Service Base Url List Uptime Creator job *********");
    }


}
