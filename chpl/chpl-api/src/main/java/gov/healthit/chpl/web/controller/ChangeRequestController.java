package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.changerequest.dao.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.changerequest.manager.ChangeRequestManager;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "change-requests")
@RestController
@RequestMapping("/change-requests")
public class ChangeRequestController {
    
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private ChangeRequestManager changeRequestManager;
    private ResourcePermissions resourcePermissions;
    
    @Autowired
    public ChangeRequestController(final ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO, 
            final ChangeRequestManager changeRequestManager, final ResourcePermissions resourcePermissions) {
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.changeRequestManager = changeRequestManager;
        this.resourcePermissions = resourcePermissions;
    }
    
    @ApiOperation(value = "Get details about a specific change request.", 
            notes="")
    @RequestMapping(value = "/{changeRequestId}", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ChangeRequest getChangeRequest(@PathVariable final Long changeRequestId) throws EntityRetrievalException {
        if (!resourcePermissions.isUserRoleDeveloperAdmin()) {
            throw new NotImplementedException();
        }
        return changeRequestManager.getChangeRequest(changeRequestId);
    }
    
    @ApiOperation(value = "Get details about all change requests.", 
            notes="")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ChangeRequest> getAllChangeRequests() throws EntityRetrievalException {
        if (!resourcePermissions.isUserRoleDeveloperAdmin()) {
            throw new NotImplementedException();
        }
        return changeRequestManager.getAllChangeRequestsForUser();
    }
    
    @ApiOperation(value = "Create a new change request.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ONC to create a new testing lab.")
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public ChangeRequest createChangeRequest(@RequestBody final ChangeRequest cr ) throws EntityRetrievalException, ValidationException {
        return changeRequestManager.createChangeRequest(cr);
    }
    
    @ApiOperation(value = "Update an existing request status or request details.",
            notes = "")
    @RequestMapping(value = "/{changeRequestId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public ChangeRequest updateChangeRequest(@RequestBody final ChangeRequest cr) throws EntityRetrievalException, ValidationException, EntityCreationException {
        if (!resourcePermissions.isUserRoleDeveloperAdmin()) {
            throw new NotImplementedException();
        }
        return changeRequestManager.updateChangeRequest(cr);
    }
    
}
