package gov.healthit.chpl.app;

import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.util.Date;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.util.StringUtils;

/**
 * Used to manually trigger a job that otherwise is on a cron schedule.
 * @author alarned
 *
 */
public final class TriggerJob {
    private TriggerJob() { }

    /**
     * Triggers a job to run once.
     * @param args if no arguments are specified, returns all system jobs
     *  if one argument, takes that as the job name, and defaults to "systemJobs" for the job group
     *  if two arguments, uses the second as the job group
     */
    public static void main(final String[] args) {
        String jobName = null;
        String jobGroup = "systemJobs";
        switch (args.length) {
        case 0:
            System.out.println("Job names:");
            break;
        case 2:
            jobGroup = args[1];
        case 1:
            jobName = args[0];
            break;
        default:
            System.out.println("Expects 0, 1, or 2 arguments");
            System.exit(1);
        }
        if (!StringUtils.isEmpty(jobName) && !StringUtils.isEmpty(jobGroup)) {
            try {
                StdSchedulerFactory sf = new StdSchedulerFactory();
                sf.initialize("quartz.properties");
                Scheduler scheduler = sf.getScheduler();

                TriggerKey triggerId = triggerKey("triggerJobNow_" + new Date().getTime(), "triggerJob");
                JobKey jobId = jobKey(jobName, jobGroup);

                Trigger qzTrigger = newTrigger()
                        .withIdentity(triggerId)
                        .startNow()
                        .forJob(jobId)
                        .build();
                scheduler.scheduleJob(qzTrigger);
                scheduler.shutdown();
            } catch (SchedulerException se) {
                se.printStackTrace();
            }
        } else {
            try {
                StdSchedulerFactory sf = new StdSchedulerFactory();
                sf.initialize("quartz.properties");
                Scheduler scheduler = sf.getScheduler();
                for (String group: scheduler.getJobGroupNames()) {
                    for (JobKey jobKey : scheduler.getJobKeys(groupEquals(group))) {
                        System.out.println("Found job: \"" + jobKey.getName() + "\" in group: \"" + jobKey.getGroup() + "\"");
                    }
                }
                scheduler.shutdown();
            } catch (SchedulerException se) {
                se.printStackTrace();
            }
        }
    }
}
