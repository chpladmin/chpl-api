package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.dao.changerequest.ChangeRequestStatusTypeDAO;
import gov.healthit.chpl.domain.changerequest.ChangeRequestStatusType;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "products")
@RestController
@RequestMapping("/web-change-request")
public class WebChangeRequestController {
    
    private ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO; 
    
    @Autowired
    public WebChangeRequestController(final ChangeRequestStatusTypeDAO changeRequestStatusTypeDAO) {
        this.changeRequestStatusTypeDAO = changeRequestStatusTypeDAO;
    }
    
    @ApiOperation(value = "List all change request types", 
            notes="")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ChangeRequestStatusType> getChangeRequestStatusTypes() {
        return changeRequestStatusTypeDAO.getChangeRequestStatusTypes();
    }
}
