package gov.healthit.chpl.scheduler.job.onetime;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import lombok.extern.log4j.Log4j2;

@Log4j2(topic = "curesUpdateActivityRepairJobLogger")
public class CuresUpdateActivityRepairJob implements Job {


    @SuppressWarnings("checkstyle:linelength")
    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Cures Update Activity Repair job *********");

        LOGGER.info("********* Completed the Cures Update Activity Repair job *********");

    }

    @Component("updatableActivityDao")
    private static class UpdatableActivityDAO extends BaseDAOImpl {

    }
}
