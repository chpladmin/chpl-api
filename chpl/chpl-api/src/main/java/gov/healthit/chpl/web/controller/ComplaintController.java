package gov.healthit.chpl.web.controller;

import java.util.List;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.manager.ComplaintManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.web.controller.results.ComplaintResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "complaints")
@RestController
@RequestMapping("/complaints")
@Loggable
public class ComplaintController {
    private ComplaintManager complaintManager;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public ComplaintController(final ComplaintManager complaintManager, final ErrorMessageUtil errorMessageUtil) {
        this.complaintManager = complaintManager;
        this.errorMessageUtil = errorMessageUtil;
    }

    @ApiOperation(value = "List all complaints the current user can view/edit.",
            notes = "Security Restrictions: Only complaints owned by the current user's ACB will be returned")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintResults getComplaints() {
        ComplaintResults results = new ComplaintResults();
        List<Complaint> complaints = complaintManager.getAllComplaints();
        results.setResults(complaints);
        return results;
    }

    @ApiOperation(value = "Save complaint for use in Surveillance Quarterly Report.",
            notes = "")
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody Complaint create(@RequestBody final Complaint complaint) throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException error = new ValidationException();
        //Make sure there is an ACB
        if (complaint.getCertificationBody() == null || complaint.getCertificationBody().getId() == null) {
            error.getErrorMessages().add(errorMessageUtil.getMessage("complaints.create.acbRequired"));
            throw error;
        }

        return complaintManager.create(complaint);
    }

    @ApiOperation(value = "Update complaint for use in Surveillance Quarterly Report.",
            notes = "")
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody Complaint update(@RequestBody final Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException error = new ValidationException();
        if (complaint.getCertificationBody() == null || complaint.getCertificationBody().getId() == null) {
            error.getErrorMessages().add(errorMessageUtil.getMessage("complaints.update.acbRequired"));
            throw error;
        }
        return complaintManager.update(complaint);
    }

    @ApiOperation(value = "Delete complaint for use in Surveillance Quarterly Report.",
            notes = "")
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public @ResponseBody void delete(@PathVariable("complaintId") final Long complaintId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        complaintManager.delete(complaintId);
    }
}
