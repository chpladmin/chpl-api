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

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.changerequest.search.ChangeRequestSearchRequest;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
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

    @Operation(summary = "Get details about a specific change request.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests.  ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{changeRequestId:^-?\\d+$}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = ChangeRequest.class, friendlyUrl = "/change-requests/{changeRequestId}")
    public @ResponseBody ChangeRequest getChangeRequest(@PathVariable final Long changeRequestId) throws EntityRetrievalException {
        return changeRequestManager.getChangeRequest(changeRequestId);
    }

    @Operation(summary = "Create a report with change requests that is emailed to the logged-in user based on a set of filters.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests. ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer. ROLE_DEVELOPER can get "
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
            description = "Security Restrictions: ROLE_DEVELOPER can create change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = ChangeRequestResults.class, httpMethod = "POST", friendlyUrl = "/change-requests")
    public ChangeRequestResults createChangeRequest(@RequestBody final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException {
        List<ChangeRequest> createdCrs = List.of(changeRequestManager.createChangeRequest(cr));
        ChangeRequestResults results = new ChangeRequestResults();
        results.getResults().addAll(createdCrs);
        return results;
    }

    @Operation(summary = "Update an existing request status or request details.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can update all chnage requests.  ROLE_ACB can update change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can update "
                    + "change requests where they have administrative authority based on the developer.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    @DeprecatedApiResponseFields(responseClass = ChangeRequest.class, httpMethod = "PUT", friendlyUrl = "/change-requests")
    public ChangeRequest updateChangeRequest(@RequestBody final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {
        return changeRequestManager.updateChangeRequest(cr);
    }
}
