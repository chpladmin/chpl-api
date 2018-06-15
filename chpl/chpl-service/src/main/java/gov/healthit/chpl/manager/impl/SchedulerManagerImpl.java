package gov.healthit.chpl.manager.impl;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobKey.jobKey;
import static org.quartz.TriggerBuilder.newTrigger;
import static org.quartz.TriggerKey.triggerKey;
import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.util.ArrayList;
import java.util.List;

import org.quartz.CronTrigger;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.concept.ScheduleTypeConcept;
import gov.healthit.chpl.domain.schedule.ChplTrigger;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.web.controller.exception.ValidationException;

/**
 * Implementation of Scheduler Manager.
 * @author alarned
 *
 */
@Service
public class SchedulerManagerImpl implements SchedulerManager {
    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ChplTrigger createTrigger(final ChplTrigger trigger) throws SchedulerException, ValidationException {
        Scheduler scheduler = getScheduler();
        TriggerKey triggerId;
        JobKey jobId;
        switch (trigger.getScheduleType()) {
        case CACHE_STATUS_AGE_NOTIFICATION:
            triggerId = triggerKey(trigger.getEmail().replaceAll("\\.",  "_"), "cacheStatusAgeTrigger");
            jobId = jobKey("cacheStatusAgeJob", "chplJobs");
            break;
        default:
            throw new ValidationException("invalid data");
        }

        Trigger qzTrigger = newTrigger()
                .withIdentity(triggerId)
                .startNow()
                .forJob(jobId)
                .usingJobData("email", trigger.getEmail())
                .withSchedule(cronSchedule(trigger.getCronSchedule()))
                .build();
        scheduler.scheduleJob(qzTrigger);

        ChplTrigger newTrigger = new ChplTrigger((CronTrigger) scheduler.getTrigger(triggerId));
        newTrigger.setScheduleType(ScheduleTypeConcept.CACHE_STATUS_AGE_NOTIFICATION);
        return newTrigger;
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public void deleteTrigger(final String scheduleType, final String triggerId)
            throws SchedulerException, ValidationException {
        Scheduler scheduler = getScheduler();
        TriggerKey triggerKey;
        switch (scheduleType) {
        case "CACHE_STATUS_AGE_NOTIFICATION":
            triggerKey = triggerKey(triggerId, "cacheStatusAgeTrigger");
            break;
        default:
            throw new ValidationException("invalid data");
        }
        scheduler.unscheduleJob(triggerKey);
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public List<ChplTrigger> getAllTriggers() throws SchedulerException {
        ArrayList<ChplTrigger> triggers = new ArrayList<ChplTrigger>();
        Scheduler scheduler = getScheduler();
        for (String group: scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(groupEquals(group))) {
                if (scheduler.getTrigger(triggerKey).getJobKey().getGroup().equalsIgnoreCase("chplJobs")) {
                    ChplTrigger newTrigger = new ChplTrigger((CronTrigger) scheduler.getTrigger(triggerKey));
                    switch (newTrigger.getJobName()) {
                    case "cacheStatusAgeJob":
                        newTrigger.setScheduleType(ScheduleTypeConcept.CACHE_STATUS_AGE_NOTIFICATION);
                        break;
                    default:
                        break;
                    }
                    triggers.add(newTrigger);
                }
            }
        }
        return triggers;
    }

    @Override
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public ChplTrigger updateTrigger(final ChplTrigger trigger) throws SchedulerException, ValidationException {
        Scheduler scheduler = getScheduler();
        Trigger oldTrigger;
        switch (trigger.getScheduleType()) {
        case CACHE_STATUS_AGE_NOTIFICATION:
            oldTrigger = scheduler.getTrigger(triggerKey(trigger.getName(), "cacheStatusAgeTrigger"));
            break;
        default:
            throw new ValidationException("invalid data");
        }
        Trigger qzTrigger = newTrigger()
                .withIdentity(oldTrigger.getKey())
                .startNow()
                .forJob(oldTrigger.getJobKey())
                .usingJobData(oldTrigger.getJobDataMap())
                .withSchedule(cronSchedule(trigger.getCronSchedule()))
                .build();
        scheduler.rescheduleJob(oldTrigger.getKey(), qzTrigger);

        ChplTrigger newTrigger = new ChplTrigger((CronTrigger) qzTrigger);
        newTrigger.setScheduleType(ScheduleTypeConcept.CACHE_STATUS_AGE_NOTIFICATION);
        return newTrigger;
    }

    private Scheduler getScheduler() throws SchedulerException {
        StdSchedulerFactory sf = new StdSchedulerFactory();
        sf.initialize("quartz.properties");
        Scheduler scheduler = sf.getScheduler();
        return scheduler;
    }
}
