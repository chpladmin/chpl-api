package gov.healthit.chpl.manager;

import java.util.List;

import org.quartz.SchedulerException;

import gov.healthit.chpl.domain.schedule.ChplTrigger;

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
    List<ChplTrigger> getAllTriggers() throws SchedulerException;
}
