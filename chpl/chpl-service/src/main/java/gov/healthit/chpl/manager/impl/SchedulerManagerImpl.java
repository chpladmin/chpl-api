package gov.healthit.chpl.manager.impl;

import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronTrigger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import gov.healthit.chpl.domain.schedule.CacheStatusAgeTrigger;
import gov.healthit.chpl.domain.schedule.ChplTrigger;
import gov.healthit.chpl.manager.SchedulerManager;

/**
 * Implementation of Scheduler Manager.
 * @author alarned
 *
 */
@Service
public class SchedulerManagerImpl implements SchedulerManager {
    private static final Logger LOGGER = LogManager.getLogger(SchedulerManagerImpl.class);

    /** {@inheritDoc} */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
    public List<ChplTrigger> getAllTriggers() throws SchedulerException {
        ArrayList<ChplTrigger> triggers = new ArrayList<ChplTrigger>();
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        for (String group: scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(groupEquals(group))) {
                if (scheduler.getTrigger(triggerKey).getJobKey().getName().equalsIgnoreCase("cacheStatusAgeJob")) {
                    triggers.add(new CacheStatusAgeTrigger((CronTrigger) scheduler.getTrigger(triggerKey)));
                }
            }
        }
        return triggers;
    }
}
