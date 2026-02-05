package gov.healthit.chpl.web.controller;

import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import tools.jackson.core.JacksonException;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestType;
import gov.healthit.chpl.changerequest.domain.ChangeRequestUpdateRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.ChangeRequestResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "change-requests", description = "Management of change requests.")
@RestController
@RequestMapping("/change-requests")
public class ChangeRequestController {

    private ChangeRequestManager changeRequestManager;

    @Autowired
    public ChangeRequestController(ChangeRequestManager changeRequestManager) {
        this.changeRequestManager = changeRequestManager;
    }

    @Operation(summary = "Get all available change request types  in the system",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ChangeRequestType> getChangeRequestTypes() {
        return changeRequestManager.getChangeRequestTypes();
    }

    @Operation(summary = "Get all available change request status types in the system",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/status-types", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ChangeRequestStatusType> getChangeRequestStatusTypes() {
        return changeRequestManager.getChangeRequestStatusTypes();
    }

    @Operation(summary = "Get details about a specific change request.",
            description = "Security Restrictions: Users with either role chpl-admin or chpl-onc can get all change requests. "
                    + "Users with role chpl-onc-acb can get change requests for developers where they manage at least one "
                    + "certified product for the developer. Users with role chpl-developer can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{changeRequestId:^-?\\d+$}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChangeRequest getChangeRequest(@PathVariable final Long changeRequestId) throws EntityRetrievalException {
        return changeRequestManager.getChangeRequest(changeRequestId);
    }

    @Operation(summary = "Create a report with change requests that is emailed to the logged-in user based on a set of filters.",
            description = "Security Restrictions: Users with either role chpl-admin or chpl-onc can get all change requests. "
                    + "Users with role chpl-onc-acb can get change requests for developers where they manage at least one "
                    + "certified product for the developer. Users with role chpl-developer can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/report-request", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplOneTimeTrigger triggerChangeRequestsReport(@RequestBody ChangeRequestSearchRequest searchRequest)
                    throws EntityRetrievalException, ValidationException, SchedulerException {
        return changeRequestManager.triggerChangeRequestsReport(searchRequest);
    }

    @Operation(summary = "Create a new change request.",
            description = "Security Restrictions: Users with role chpl-developer can create change requests where they "
                    + "have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ChangeRequestResults createChangeRequest(@RequestBody final ChangeRequest cr)
            throws InvalidArgumentsException, EntityRetrievalException, ValidationException, ActivityException {

        List<ChangeRequest> createdCrs = List.of(changeRequestManager.createChangeRequest(cr));
        ChangeRequestResults results = new ChangeRequestResults();
        results.getResults().addAll(createdCrs);
        return results;
    }

    @Operation(summary = "Update an existing request status or request details.",
            description = "Security Restrictions: Users with either role chpl-admin or chpl-onc can update all chnage "
                    + "requests. Users with role chpl-onc-acb can update change requests for developers where they manage at "
                    + "least one certified product for the developer.  Users with role chpl-developer can update "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public ChangeRequest updateChangeRequest(@RequestBody final ChangeRequestUpdateRequest updateRequest)
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JacksonException, ActivityException, InvalidArgumentsException, EmailNotSentException {
        return changeRequestManager.updateChangeRequest(updateRequest);
    }
}
