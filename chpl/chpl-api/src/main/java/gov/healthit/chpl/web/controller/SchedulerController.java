package gov.healthit.chpl.web.controller;

import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplTrigger;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.web.controller.results.ChplJobsResults;
import gov.healthit.chpl.web.controller.results.ScheduleTriggersResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * API interface for Quartz Scheduled jobs.
 * @author alarned
 *
 */
@Api(value = "schedules")
@RestController
@RequestMapping("/schedules")
public class SchedulerController {

    @Autowired
    private SchedulerManager schedulerManager;

    /**
     * Create a new Trigger based on passed information.
     * @param trigger input
     * @return the new trigger
     * @throws SchedulerException if exception is thrown
     * @throws ValidationException if job values aren't correct
     */
    @ApiOperation(value = "Create a new trigger and return it",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and have administrative authority on the specified ACB.")
    @RequestMapping(value = "/triggers", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ScheduleTriggersResults createTrigger(@RequestBody(required = true)
    final ChplTrigger trigger) throws SchedulerException, ValidationException {
        ChplTrigger result = schedulerManager.createTrigger(trigger);
        ScheduleTriggersResults results = new ScheduleTriggersResults();
        results.getResults().add(result);
        return results;
    }

    /**
     * Remove a new Trigger based on passed information.
     * @param triggerGroup The group that identifies the trigger to remove
     * @param triggerName The name that identifies the trigger to remove
     * @throws SchedulerException if exception is thrown
     * @throws ValidationException if job values aren't correct
     */
    @ApiOperation(value = "Delete an existing trigger")
    @RequestMapping(value = "/triggers/{triggerGroup}/{triggerName}", method = RequestMethod.DELETE)
    public void deleteTrigger(@PathVariable("triggerGroup") final String triggerGroup,
            @PathVariable("triggerName") final String triggerName)
                    throws SchedulerException, ValidationException {
        schedulerManager.deleteTrigger(triggerGroup, triggerName);
    }

    /**
     * Get the list of all triggers and their associated scheduled jobs that are applicable to the
     * currently logged in user.
     * @return current scheduled jobs
     * @throws SchedulerException if scheduler has an issue
     */
    @ApiOperation(value = "Get the list of all triggers and their associated scheduled jobs "
            + "that are applicable to the currently logged in user",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and have administrative "
                    + "authority on the specified ACB.")
    @RequestMapping(value = "/triggers", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ScheduleTriggersResults getAllTriggers() throws SchedulerException {
        List<ChplTrigger> triggers = schedulerManager.getAllTriggers();
        ScheduleTriggersResults results = new ScheduleTriggersResults();
        results.setResults(triggers);
        return results;
    }

    /**
     * Update an existing Trigger based on passed information.
     * @param trigger input trigger
     * @return the updated trigger
     * @throws SchedulerException if exception is thrown
     * @throws ValidationException if job values aren't correct
     */
    @ApiOperation(value = "Update an existing trigger and return it",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and have administrative authority on the specified "
                    + "ACB.")
    @RequestMapping(value = "/triggers", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody ScheduleTriggersResults updateTrigger(@RequestBody(required = true) final ChplTrigger trigger)
            throws SchedulerException, ValidationException {
        ChplTrigger result = schedulerManager.updateTrigger(trigger);
        ScheduleTriggersResults results = new ScheduleTriggersResults();
        results.getResults().add(result);
        return results;
    }

    /**
     * Returns a list of all jobs that are applicable to the currently logged in user.
     * @return List of ChplJob objects
     * @throws SchedulerException if exception is thrown
     */
    @ApiOperation(value = "Get the list of all jobs that are applicable to the currently logged in user",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, or ROLE_ACB and have administrative authority on the specified ACB")
    @RequestMapping(value = "/jobs", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplJobsResults getAllJobs() throws SchedulerException {
        List<ChplJob> jobs = schedulerManager.getAllJobs();
        ChplJobsResults results = new ChplJobsResults();
        results.setResults(jobs);
        return results;
    }

    /**
     * Update an existing Job based on passed in information.
     * @param job the CHPL job
     * @return the updated job
     * @throws SchedulerException if scheduler has issues
     */
    @ApiOperation(value = "Update a given job",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ACB and have administrative authority on the specified ACB.")
    @RequestMapping(value = "/jobs", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplJobsResults updateJob(@RequestBody(required = true)
    final ChplJob job) throws SchedulerException {
        ChplJob result = schedulerManager.updateJob(job);
        ChplJobsResults results = new ChplJobsResults();
        results.getResults().add(result);
        return results;
    }
}
