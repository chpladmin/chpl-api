package gov.healthit.chpl.manager;

import java.util.List;

import org.quartz.SchedulerException;
import org.quartz.Trigger;

/**
 * Interface for managing schedules.
 * @author alarned
 *
 */
public interface SchedulerManager {
    /**
     * Get all active Triggers.
     * @throws SchedulerException if scheduler has an issue
     * @return the triggers
     */
    List<Trigger> getAllTriggers() throws SchedulerException;
}
