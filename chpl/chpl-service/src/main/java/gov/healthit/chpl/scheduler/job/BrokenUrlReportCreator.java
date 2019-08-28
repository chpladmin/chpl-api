package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Quartz job to check every URl in the system and log its response code to the database.
 * @author kekey
 *
 */
public class BrokenUrlReportCreator extends QuartzJob implements InterruptableJob {
    private static final Logger LOGGER = LogManager.getLogger("brokenUrlReportCreatorJobLogger");
    private boolean interrupted;

    /**
     * Default constructor.
     */
    public BrokenUrlReportCreator() {
        interrupted = false;
    }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        try {
            //get all urls in the system

            //for each url in the system
                //has the url been checked before?
                //was the last check within the last <property> amount of time and successful?
                //was the last check within the last <property> amount of time and failed?

                //if we should check it again - do so

            //save the results to the report table

            //for each url in the report table
                //does it exist in the system?
                //if not, remove it

        } catch (Exception ex) {
            LOGGER.debug("Unable to complete job: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void interrupt() throws UnableToInterruptJobException {
        this.interrupted = true;
    }
}
