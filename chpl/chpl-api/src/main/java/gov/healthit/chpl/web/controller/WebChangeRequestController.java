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
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import gov.healthit.chpl.domain.changerequest.ChangeRequestWebsite;
import gov.healthit.chpl.entity.changerequest.DeveloperWebsiteChangeRequest;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.changerequest.ChangeRequestWebsiteManager;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "change-requests")
@RestController
@RequestMapping("/change-requests")
public class WebChangeRequestController {
    
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO;
    private ChangeRequestWebsiteManager changeRequestWebsiteManager;
    
    @Autowired
    public WebChangeRequestController(final ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO, final ChangeRequestWebsiteManager changeRequestWebsiteManager) {
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
        this.changeRequestWebsiteManager = changeRequestWebsiteManager;
    }
    
    @ApiOperation(value = "List all change request types", 
            notes="")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ChangeRequestStatusType> getChangeRequestStatusTypes() {
        return changeRequestStatusTypeDAO.getChangeRequestStatusTypes();
    }
    
    @ApiOperation(value = "Create a new testing lab.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ONC to create a new testing lab.")
    @RequestMapping(value = "/websites", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public ChangeRequestWebsite createChangeRequest(@RequestBody final DeveloperWebsiteChangeRequest developerWebsiteChangeRequest) throws EntityRetrievalException {
        return changeRequestWebsiteManager.create(developerWebsiteChangeRequest.getDeveloper(), developerWebsiteChangeRequest.getWebsite());
    }
    
}
