package gov.healthit.chpl.web.controller;

import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.schedule.ChplJob;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.domain.schedule.ChplRepeatableTrigger;
import gov.healthit.chpl.domain.schedule.ScheduledSystemJob;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.SchedulerManager;
import gov.healthit.chpl.web.controller.annotation.CacheControl;
import gov.healthit.chpl.web.controller.annotation.CacheMaxAge;
import gov.healthit.chpl.web.controller.annotation.CachePolicy;
import gov.healthit.chpl.web.controller.results.ChplJobsResults;
import gov.healthit.chpl.web.controller.results.ScheduleOneTimeTriggersResults;
import gov.healthit.chpl.web.controller.results.ScheduleTriggersResults;
import gov.healthit.chpl.web.controller.results.SystemTriggerResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "schedules")
@RestController
@RequestMapping("/schedules")
@Loggable
public class SchedulerController {
    private static final String USER_JOB_TYPE = "user";
    private static final String SYSTEM_JOB_TYPE = "system";

    @Autowired
    private SchedulerManager schedulerManager;

    @ApiOperation(value = "Create a new trigger and return it",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB.")
    @RequestMapping(value = "/triggers", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ScheduleTriggersResults createTrigger(@RequestBody(required = true)
        ChplRepeatableTrigger trigger) throws SchedulerException, ValidationException, EmailNotSentException {
        ChplRepeatableTrigger result = schedulerManager.createTrigger(trigger);
        ScheduleTriggersResults results = new ScheduleTriggersResults();
        results.getResults().add(result);
        return results;
    }

    @ApiOperation(value = "Create a new trigger and return it")
    @RequestMapping(value = "/triggers/one_time", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ScheduleOneTimeTriggersResults createOneTimeTrigger(@RequestBody(required = true)
        ChplOneTimeTrigger trigger) throws SchedulerException, ValidationException {
        ChplOneTimeTrigger result = schedulerManager.createOneTimeTrigger(trigger);
        ScheduleOneTimeTriggersResults results = new ScheduleOneTimeTriggersResults();
        results.getResults().add(result);
        return results;
    }

    @ApiOperation(value = "Delete an existing trigger")
    @RequestMapping(value = "/triggers/{triggerGroup}/{triggerName}", method = RequestMethod.DELETE)
    public void deleteTrigger(@PathVariable("triggerGroup") String triggerGroup,
            @PathVariable("triggerName") String triggerName)
                    throws SchedulerException, ValidationException, EmailNotSentException {
        schedulerManager.deleteTrigger(triggerGroup, triggerName);
    }

    @ApiOperation(value = "Get the list of all triggers of type '" + USER_JOB_TYPE + "' or '" + SYSTEM_JOB_TYPE + "' "
            + "that are applicable to the currently logged in user",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB for '" + USER_JOB_TYPE + "' jobs "
                    + "and have administrative authority on the specified ONC-ACB. "
                    + "ROLE_ADMIN, ROLE_ONC, or ROLE_ONC_STAFF for '" + SYSTEM_JOB_TYPE + "' jobs. "
                    + "Note: The default jobType query parameter is set to '" + USER_JOB_TYPE + "'.")
    @RequestMapping(value = "/triggers", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody Object getAllTriggersByJobType(@RequestParam(defaultValue = USER_JOB_TYPE) String jobType)
            throws SchedulerException {
        switch (jobType.toLowerCase()) {
        case USER_JOB_TYPE:
            List<ChplRepeatableTrigger> triggers = schedulerManager.getAllTriggersForUser();
            return new ScheduleTriggersResults(triggers);
        case SYSTEM_JOB_TYPE:
            List<ScheduledSystemJob> scheduledSystemJobs = schedulerManager.getScheduledSystemJobsForUser();
            return new SystemTriggerResults(scheduledSystemJobs);
        default:
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bad Request: "
                    + "Please specify a query parameter of either '" + USER_JOB_TYPE + "' or '" + SYSTEM_JOB_TYPE + "'");
        }
    }

    @ApiOperation(value = "Update an existing trigger and return it",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF or ROLE_ACB and have administrative authority on "
                    + "the specified ONC-ACB.")
    @RequestMapping(value = "/triggers", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody ScheduleTriggersResults updateTrigger(@RequestBody(required = true) ChplRepeatableTrigger trigger)
            throws SchedulerException, ValidationException, EmailNotSentException {
        ChplRepeatableTrigger result = schedulerManager.updateTrigger(trigger);
        ScheduleTriggersResults results = new ScheduleTriggersResults();
        results.getResults().add(result);
        return results;
    }

    @ApiOperation(value = "Get the list of all jobs that are applicable to the currently logged in user",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF, or ROLE_ACB and have administrative authority on the specified ONC-ACB")
    @RequestMapping(value = "/jobs", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @CacheControl(policy = CachePolicy.PUBLIC, maxAge = CacheMaxAge.TWELVE_HOURS)
    public @ResponseBody ChplJobsResults getAllJobs() throws SchedulerException {
        List<ChplJob> jobs = schedulerManager.getAllJobs();
        ChplJobsResults results = new ChplJobsResults();
        results.setResults(jobs);
        return results;
    }

    @ApiOperation(value = "Update a given job",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, ROLE_ONC_STAFF or ROLE_ACB and have administrative authority on "
                    + "the specified ONC-ACB.")
    @RequestMapping(value = "/jobs", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplJobsResults updateJob(@RequestBody(required = true) ChplJob job)
            throws SchedulerException {
        ChplJob result = schedulerManager.updateJob(job);
        ChplJobsResults results = new ChplJobsResults();
        results.getResults().add(result);
        return results;
    }
}
