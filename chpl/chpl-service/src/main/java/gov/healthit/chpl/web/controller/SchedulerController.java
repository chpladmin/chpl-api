package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.SchedulerException;
import org.quartz.TriggerKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.schedule.ChplTrigger;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.web.controller.exception.ValidationException;
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

    private static final Logger LOGGER = LogManager.getLogger(SchedulerController.class);

    @Autowired
    private SchedulerManager schedulerManager;

    @Autowired
    private MessageSource messageSource;

    /**
     * Create a new Trigger based on passed information.
     * @param trigger input
     * @return the new trigger
     * @throws SchedulerException if exception is thrown
     * @throws ValidationException if job values aren't correct
     */
    @ApiOperation(value = "Create a new trigger and return it")
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
     * @param triggerKey the trigger to delete
     * @throws SchedulerException if exception is thrown
     */
    @ApiOperation(value = "Delete an existing trigger")
    @RequestMapping(value = "/triggers/{triggerKey}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody void deleteTrigger(
            @PathVariable("triggerKey") final String triggerKey) throws SchedulerException {
        schedulerManager.deleteTrigger(triggerKey);
    }

    /**
     * Get the list of all triggers and their associated scheduled jobs that are applicable to the
     * currently logged in user.
     * @return current scheduled jobs
     * @throws SchedulerException if scheduler has an issue
     */
    @ApiOperation(value = "Get the list of all triggers and their associated scheduled jobs "
            + "that are applicable to the currently logged in user")
    @RequestMapping(value = "/triggers", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ScheduleTriggersResults getAllTriggers() throws SchedulerException {
        List<ChplTrigger> triggers = schedulerManager.getAllTriggers();
        ScheduleTriggersResults results = new ScheduleTriggersResults();
        results.setResults(triggers);
        return results;
    }
}
