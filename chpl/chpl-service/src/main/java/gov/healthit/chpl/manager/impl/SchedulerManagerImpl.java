package gov.healthit.chpl.manager.impl;

import static org.quartz.impl.matchers.GroupMatcher.groupEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

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
    public List<Trigger> getAllTriggers() throws SchedulerException {
        ArrayList<Trigger> triggers = new ArrayList<Trigger>();
        Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
        for (String group: scheduler.getTriggerGroupNames()) {
            // enumerate each trigger in group
            for (TriggerKey triggerKey : scheduler.getTriggerKeys(groupEquals(group))) {
                LOGGER.info("Found trigger identified as group: " + group + ", name: " + triggerKey.getName());
                triggers.add(scheduler.getTrigger(triggerKey));
            }
        }
        return triggers;
    }
}
