package gov.healthit.chpl.web.controller;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.complaint.ComplaintManager;
import gov.healthit.chpl.complaint.domain.Complaint;
import gov.healthit.chpl.domain.schedule.ChplOneTimeTrigger;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
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

    @Operation(summary = "Save complaint for use in Surveillance Quarterly Report.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, produces = "application/json; charset=utf-8")
    public @ResponseBody Complaint create(@RequestBody Complaint complaint) throws ValidationException, EntityRetrievalException, ActivityException {
        // Make sure there is an ACB
        if (complaint.getCertificationBody() == null || complaint.getCertificationBody().getId() == null) {
            throw new ValidationException(errorMessageUtil.getMessage("complaints.create.acbRequired"));
        }

        return complaintManager.create(complaint);
    }

    @Operation(summary = "Update complaint for use in Surveillance Quarterly Report.",
            description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{complaintId}", method = RequestMethod.PUT, produces = "application/json; charset=utf-8")
    public @ResponseBody Complaint update(@RequestBody Complaint complaint) throws ValidationException, EntityRetrievalException, ActivityException {
        if (complaint.getCertificationBody() == null || complaint.getCertificationBody().getId() == null) {
            throw new ValidationException(errorMessageUtil.getMessage("complaints.update.acbRequired"));
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
    public void delete(@PathVariable("complaintId") Long complaintId) throws EntityRetrievalException, ActivityException {
        complaintManager.delete(complaintId);
    }

    @Operation(summary = "Generate the Complaints Report and email the results to the logged-in user.",
            description = "Security Restrictions: ROLE_ADMIN and ROLE_ONC have access.",
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
