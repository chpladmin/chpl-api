package gov.healthit.chpl.manager;

import java.util.List;

import org.quartz.SchedulerException;
import org.quartz.TriggerKey;

import gov.healthit.chpl.domain.schedule.ChplTrigger;
import gov.healthit.chpl.web.controller.exception.ValidationException;

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
     * @param triggerId existing trigger ID
     * @throws SchedulerException if scheduler has an issue
     */
    void deleteTrigger(String triggerId) throws SchedulerException;

    /**
     * Get all active Triggers.
     * @throws SchedulerException if scheduler has an issue
     * @return the triggers
     */
    List<ChplTrigger> getAllTriggers() throws SchedulerException;
}
