package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.dao.changerequest.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.domain.changerequest.ChangeRequest;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.changerequest.ChangeRequestManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "web-change-requests")
@RestController
@RequestMapping("/web-change-requests")
public class WebChangeRequestController {
    
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private ChangeRequestManager changeRequestManager;
    
    @Autowired
    public WebChangeRequestController(final ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO, final ChangeRequestManager changeRequestManager) {
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.changeRequestManager = changeRequestManager;
    }
    
    @ApiOperation(value = "List all change request types", 
            notes="")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ChangeRequestStatusType> getChangeRequestStatusTypes() {
        return changeRequestStatusTypeDAO.getChangeRequestStatusTypes();
    }
    
    @ApiOperation(value = "Create a new testing lab.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ONC to create a new testing lab.")
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public ChangeRequest createChangeRequest(@RequestBody final ChangeRequest cr) throws EntityRetrievalException {
        return changeRequestManager.create(cr);
    }
    
}
