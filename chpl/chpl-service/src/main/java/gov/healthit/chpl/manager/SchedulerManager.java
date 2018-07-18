package gov.healthit.chpl.manager;

import java.util.List;

import org.quartz.SchedulerException;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplTrigger;
import gov.healthit.chpl.exception.ValidationException;

/**
 * Interface for managing schedules.
 * @author alarned
 *
 */
public interface SchedulerManager {
    /**
     * Create a new Trigger.
     * @param trigger the CHPL Trigger to create
     * @throws SchedulerException if scheduler has an issue
     * @throws ValidationException if job values aren't correct
     * @return the new trigger
     */
    ChplTrigger createTrigger(ChplTrigger trigger) throws SchedulerException, ValidationException;

    /**
     * Delete an existing Trigger.
     * @param scheduleType type of schedule
     * @param triggerId existing trigger ID
     * @throws SchedulerException if scheduler has an issue
     * @throws ValidationException if job values aren't correct
     */
    void deleteTrigger(String scheduleType, String triggerId) throws SchedulerException, ValidationException;

    /**
     * Get all active Triggers.
     * @throws SchedulerException if scheduler has an issue
     * @return the triggers
     */
    List<ChplTrigger> getAllTriggers() throws SchedulerException;

    /**
     * Update trigger with new data.
     * @param trigger old trigger
     * @return updated trigger
     * @throws SchedulerException if scheduler has issue
     * @throws ValidationException if job values aren't correct
     */
    ChplTrigger updateTrigger(ChplTrigger trigger) throws SchedulerException, ValidationException;
    
    List<ChplJob> getAllJobs() throws SchedulerException;
}
