package gov.healthit.chpl.web.controller;

import java.util.List;

import org.apache.commons.lang.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.ComplaintDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ComplaintManager;
import gov.healthit.chpl.web.controller.results.ComplaintResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "complaints")
@RestController
@RequestMapping("/complaints")
public class ComplaintController {
    private ComplaintManager complaintManager;
    private FF4j ff4j;
    
    @Autowired
    public ComplaintController(final ComplaintManager complaintManager, final FF4j ff4j) {
        this.complaintManager = complaintManager;
        this.ff4j = ff4j;
    }
    
    @ApiOperation(value = "List all complaints the current user can view/edit.",
            notes = "Security Restrictions: Only complaints owned by the current user's ACB will be returned")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintResults getComplaints() {
        if (ff4j.check(FeatureList.COMPLAINTS)) {
            ComplaintResults results = new ComplaintResults();
            List<ComplaintDTO> dtos = complaintManager.getAllComplaints();
            results.setResults(dtos);
            return results;
        } else {
            throw new NotImplementedException();
        }
    }
    
    @ApiOperation(value = "Save complaint for use in Surveillance Quarterly Report.",
            notes = "")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintDTO create(@RequestBody final ComplaintDTO complaint) throws EntityRetrievalException {
        if (ff4j.check(FeatureList.COMPLAINTS)) {
            ComplaintDTO dto = complaintManager.create(complaint);
            return dto;
        } else {
            throw new NotImplementedException();
        }
    }

    @ApiOperation(value = "Update complaint for use in Surveillance Quarterly Report.",
            notes = "")
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintDTO update(@RequestBody final ComplaintDTO complaint) throws EntityRetrievalException {
        if (ff4j.check(FeatureList.COMPLAINTS)) {
            ComplaintDTO dto = complaintManager.update(complaint);
            return dto;
        } else {
            throw new NotImplementedException();
        }
    }
    
    @ApiOperation(value = "Delete complaint for use in Surveillance Quarterly Report.",
            notes = "")
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public @ResponseBody void delete(@PathVariable("complaintId") final Long complaintId) throws EntityRetrievalException {
        if (ff4j.check(FeatureList.COMPLAINTS)) { 
            complaintManager.delete(complaintId);
        } else {
            throw new NotImplementedException();
        }
    }
}
