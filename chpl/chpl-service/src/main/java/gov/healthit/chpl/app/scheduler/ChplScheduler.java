package gov.healthit.chpl.app.scheduler;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import gov.healthit.chpl.app.CacheStatusAgeApp;

public final class ChplScheduler {
    private static final Logger LOGGER = LogManager.getLogger(ChplScheduler.class);

    private ChplScheduler() {
        //Default private constructor
    }

    public static void main(final String[] args) {
        try {
            Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
            scheduler.start();

            JobDetail cacheStatusAgeAppJob = newJob(CacheStatusAgeApp.class)
                    .withIdentity("cacheStatusAgeAppJob", "group1")
                    .build();

            Trigger cacheStatusAgeAppTrigger = newTrigger()
                    .withIdentity("cacheStatusAgeAppTrigger", "group1")
                    .startNow()
                    .withSchedule(cronSchedule("0 13 * * * *"))
                    .build();

            // Tell quartz to schedule the job using our trigger
            scheduler.scheduleJob(cacheStatusAgeAppJob, cacheStatusAgeAppTrigger);

        } catch (SchedulerException se) {
            se.printStackTrace();
        }
    }

}

