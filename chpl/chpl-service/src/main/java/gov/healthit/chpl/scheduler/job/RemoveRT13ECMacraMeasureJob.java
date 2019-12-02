package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import gov.healthit.chpl.dao.MacraMeasureDAO;

public class RemoveRT13ECMacraMeasureJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("removeRT13ECMacraMeasureJobLogger");

    private static final String RT_13_EC = "RT 13 EC";

    @Autowired
    private MacraMeasureDAO macraMeasureDAO;

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        LOGGER.info("********* Starting the Remove Criteria job. *********");
        macraMeasureDAO.remove(RT_13_EC);
        LOGGER.info("********* Completed the Remove Criteria job. *********");
    }
}
