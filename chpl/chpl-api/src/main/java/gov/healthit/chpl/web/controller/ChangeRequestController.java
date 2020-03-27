package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
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
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.web.controller.results.ChangeRequestResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "change-requests")
@RestController
@RequestMapping("/change-requests")
public class ChangeRequestController {

    private ChangeRequestManager changeRequestManager;
    private FF4j ff4j;

    @Autowired
    public ChangeRequestController(final ChangeRequestManager changeRequestManager, final FF4j ff4j) {
        this.changeRequestManager = changeRequestManager;
        this.ff4j = ff4j;
    }

    @ApiOperation(value = "Get details about a specific change request.",
            notes="Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests.  ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.")
    @RequestMapping(value = "/{changeRequestId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChangeRequest getChangeRequest(@PathVariable final Long changeRequestId) throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.CHANGE_REQUEST)) {
            throw new NotImplementedException();
        }

        return changeRequestManager.getChangeRequest(changeRequestId);
    }

    @ApiOperation(value = "Get details about all change requests.",
            notes = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can get all change requests.  ROLE_ACB can get change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can get "
                    + "change requests where they have administrative authority based on the developer.")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ChangeRequest> getAllChangeRequests() throws EntityRetrievalException {
        if (!ff4j.check(FeatureList.CHANGE_REQUEST)) {
            throw new NotImplementedException();
        }
        return changeRequestManager.getAllChangeRequestsForUser();
    }

    @ApiOperation(value = "Create a new change request.",
            notes = "Security Restrictions: ROLE_DEVELOPER can create change requests where they have administrative authority based on the developer.")
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public ChangeRequestResults createChangeRequest(@RequestBody final ChangeRequest cr )
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException,
            InvalidArgumentsException {
        if (!ff4j.check(FeatureList.CHANGE_REQUEST)) {
            throw new NotImplementedException();
        }

        List<ChangeRequest> createdCrs = changeRequestManager.createChangeRequests(cr);
        ChangeRequestResults results = new ChangeRequestResults();
        results.getResults().addAll(createdCrs);
        return results;
    }

    @ApiOperation(value = "Update an existing request status or request details.",
            notes = "Security Restrictions: ROLE_ADMIN & ROLE_ONC can update all chnage requests.  ROLE_ACB can update change requests "
                    + "for developers where they manage at least one certified product for the developer.  ROLE_DEVELOPER can update "
                    + "change requests where they have administrative authority based on the developer.")
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public ChangeRequest updateChangeRequest(@RequestBody final ChangeRequest cr) throws EntityRetrievalException, ValidationException, EntityCreationException, JsonProcessingException {
        if (!ff4j.check(FeatureList.CHANGE_REQUEST)) {
            throw new NotImplementedException();
        }

        return changeRequestManager.updateChangeRequest(cr);
    }
}
