package gov.healthit.chpl.web.controller;

import java.util.List;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ComplaintManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedResponseFields;
import gov.healthit.chpl.web.controller.results.ComplaintResults;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "complaints", description = "Allows management of complaints.")
@RestController
@RequestMapping("/complaints")
public class ComplaintController {
    private ComplaintManager complaintManager;
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    public ComplaintController(ComplaintManager complaintManager, ErrorMessageUtil errorMessageUtil) {
        this.complaintManager = complaintManager;
        this.errorMessageUtil = errorMessageUtil;
    }

    @Operation(summary = "List all complaints the current user can view.",
            description = "Security Restrictions: Only complaints owned by the current user's ACB will be returned",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedResponseFields(responseClass = ComplaintResults.class)
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody ComplaintResults getComplaints() {
        ComplaintResults results = new ComplaintResults();
        List<Complaint> complaints = complaintManager.getAllComplaints();
        results.setResults(complaints);
        return results;
    }

    @Operation(summary = "Save complaint for use in Surveillance Quarterly Report.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedResponseFields(responseClass = Complaint.class)
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody Complaint create(@RequestBody Complaint complaint) throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException error = new ValidationException();
        // Make sure there is an ACB
        if (complaint.getCertificationBody() == null || complaint.getCertificationBody().getId() == null) {
            error.getErrorMessages().add(errorMessageUtil.getMessage("complaints.create.acbRequired"));
            throw error;
        }

        return complaintManager.create(complaint);
    }

    @Operation(summary = "Update complaint for use in Surveillance Quarterly Report.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @DeprecatedResponseFields(responseClass = Complaint.class)
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody Complaint update(@RequestBody Complaint complaint)
            throws EntityRetrievalException, ValidationException, JsonProcessingException, EntityCreationException {
        ValidationException error = new ValidationException();
        if (complaint.getCertificationBody() == null || complaint.getCertificationBody().getId() == null) {
            error.getErrorMessages().add(errorMessageUtil.getMessage("complaints.update.acbRequired"));
            throw error;
        }
        return complaintManager.update(complaint);
    }

    @Operation(summary = "Delete complaint for use in Surveillance Quarterly Report.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("complaintId") Long complaintId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        complaintManager.delete(complaintId);
    }

    @Operation(summary = "Generate the Complaints Report and email the results to the logged-in user.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC, and ROLE_ONC_STAFF have access.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/report-request", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody ChplOneTimeTrigger triggerComplaintsReport() throws SchedulerException, ValidationException {
        ChplOneTimeTrigger jobTrigger = complaintManager.triggerComplaintsReport();
        return jobTrigger;
    }
}
