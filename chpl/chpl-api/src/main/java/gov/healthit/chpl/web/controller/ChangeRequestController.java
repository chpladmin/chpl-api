package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.results.ChangeRequestResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "change-requests", description = "Management of change requests.")
@RestController
@RequestMapping("/change-requests")
@Loggable
public class ChangeRequestController {

    private ChangeRequestManager changeRequestManager;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public ChangeRequestController(ChangeRequestManager changeRequestManager, ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.changeRequestManager = changeRequestManager;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Operation(summary = "Get details about a specific change request.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests.  ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "/{changeRequestId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChangeRequest getChangeRequest(@PathVariable final Long changeRequestId) throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.CHANGE_REQUEST)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }

        return changeRequestManager.getChangeRequest(changeRequestId);
    }

    @Operation(summary = "Get details about all change requests.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests.  ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ChangeRequest> getAllChangeRequests() throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.CHANGE_REQUEST)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }
        return changeRequestManager.getAllChangeRequestsForUser();
    }

    @Operation(summary = "Create a new change request.",
            description = "Security Restrictions: ROLE_DEVELOPER can create change requests where they have administrative authority based on the developer.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/json; charset=utf-8")
    public ChangeRequestResults createChangeRequest(@RequestBody final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException {
        if (!ff4j.check(FeatureList.CHANGE_REQUEST)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }

        List<ChangeRequest> createdCrs = changeRequestManager.createChangeRequests(cr);
        ChangeRequestResults results = new ChangeRequestResults();
        results.getResults().addAll(createdCrs);
        return results;
    }

    @Operation(summary = "Update an existing request status or request details.",
            description = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can update all chnage requests.  ROLE_ACB can update change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can update "
                    + "change requests where they have administrative authority based on the developer.",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)})
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/json; charset=utf-8")
    public ChangeRequest updateChangeRequest(@RequestBody final ChangeRequest cr)
            throws EntityRetrievalException, ValidationException, EntityCreationException,
            JsonProcessingException, InvalidArgumentsException, EmailNotSentException {
        if (!ff4j.check(FeatureList.CHANGE_REQUEST)) {
            throw new NotImplementedException(msgUtil.getMessage("notImplemented"));
        }

        return changeRequestManager.updateChangeRequest(cr);
    }
}
