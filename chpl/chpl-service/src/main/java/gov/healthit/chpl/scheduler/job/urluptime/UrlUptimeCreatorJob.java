package gov.healthit.chpl.scheduler.job.urluptime;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.scheduler.job.QuartzJob;
import lombok.extern.log4j.Log4j2;

//@Log4j2(topic =  "urlUptimeCreatorJobLogger")
@Log4j2
public class UrlUptimeCreatorJob extends QuartzJob {

    @Autowired
    private DatadogChplSynchonizer datadogChplSynchonizer;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Url Uptime Creator job *********");
        try {
            datadogChplSynchonizer.synchronizeServiceBasedUrlAndDatadogApiTests();
        } catch (Exception e) {
            LOGGER.error(e);
        }
        LOGGER.info("********* Completed the Url Uptime Creator job *********");
    }


}
