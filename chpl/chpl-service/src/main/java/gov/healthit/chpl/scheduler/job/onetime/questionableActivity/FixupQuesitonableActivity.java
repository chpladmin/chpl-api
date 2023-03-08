package gov.healthit.chpl.scheduler.job.onetime.questionableActivity;

import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import lombok.extern.log4j.Log4j2;

@DisallowConcurrentExecution
@Log4j2(topic = "fixupQuestionableActivityJobLogger")
public class FixupQuesitonableActivity  implements Job {
    @Override
    public void execute(JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        LOGGER.info("********* Starting the Fixup Questionable Activity job. *********");

        LOGGER.info("********* Completed the Fixup Questionable Activity job. *********");
    }
}
