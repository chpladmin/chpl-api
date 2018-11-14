package gov.healthit.chpl.scheduler.job;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.UnableToInterruptJobException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

/**
 * Quartz job used to interrupt a different job.
 * @author alarned
 *
 */
public class InterruptJob extends QuartzJob {
    private static final Logger LOGGER = LogManager.getLogger("interruptJobLogger");

    /**
     * Default constructor.
     */
    public InterruptJob() { }

    @Override
    public void execute(final JobExecutionContext jobContext) throws JobExecutionException {
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);

        String jobName = jobContext.getMergedJobDataMap().getString("jobName");
        String jobGroup = jobContext.getMergedJobDataMap().getString("jobGroup");
        Scheduler scheduler = jobContext.getScheduler();

        if (StringUtils.isEmpty(jobName)) {
            throw new JobExecutionException("Job name is blank");
        }
        if (StringUtils.isEmpty(jobGroup)) {
            jobGroup = "systemJobs";
        }
        LOGGER.info("Interrupting job with name \"{}\" in group \"{}\"", jobName, jobGroup);
        JobKey jobKey = new JobKey(jobName, jobGroup);
        try {
            scheduler.interrupt(jobKey);
        } catch (UnableToInterruptJobException e) {
            LOGGER.error("Unable to interrupt job with name \"{}\" in group \"{}\"", jobName, jobGroup);
        }
    }
}
