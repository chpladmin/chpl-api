package gov.healthit.chpl.app.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

public final class ChplScheduler {
    private static final Logger LOGGER = LogManager.getLogger(ChplScheduler.class);

    private ChplScheduler() {
        //Default private constructor
    }

    public static void main(final String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

//            JobDetail cacheStatusAgeJob = newJob(CacheStatusAgeJob.class)
//                    .withIdentity("cacheStatusAgeJob", "group1")
//                    .storeDurably()
//                    .build();
//            scheduler.addJob(cacheStatusAgeJob, true);
//
//            Trigger cacheStatusAgeTrigger = newTrigger()
//                    .withIdentity("cacheStatusAgeTrigger-alarned@ainq.com", "group1")
//                    .startNow()
//                    .forJob(jobKey("cacheStatusAgeJob", "group1"))
//                    .usingJobData("email", "alarned@ainq.com")
//                    .withSchedule(cronSchedule("0 13 * * * ?"))
//                    .build();
//
//            scheduler.scheduleJob(cacheStatusAgeTrigger);

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

}

