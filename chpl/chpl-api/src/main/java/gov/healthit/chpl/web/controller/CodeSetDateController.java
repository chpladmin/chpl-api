package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.codesetdate.CodeSetDate;
import gov.healthit.chpl.codesetdate.CodeSetDateManager;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "code-set-date", description = "Endpoints related to Code Set Dates.")
@RestController
@RequestMapping("/code-set-date")
public class CodeSetDateController {
    private CodeSetDateManager codeSetDateManager;

    @Autowired
    public CodeSetDateController(CodeSetDateManager codeSetDateManager) {
        this.codeSetDateManager = codeSetDateManager;
    }

    @Operation(summary = "Retrieve all Code Set Dates. ",
            description = "Returns all of the Code Set Datess that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CodeSetDate> getAllCodeSetDates() {
        return codeSetDateManager.getAll();
    }

    @Operation(summary = "Get all criteria that Code Set Dates can be associated with.",
            description = "Returns all of the Criteria that a Code Set Dates can be associated to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForCodeSetDates() {
        return codeSetDateManager.getCertificationCriteriaForCodeSetDates();
    }

    @Operation(summary = "Create a Code Set Dates.",
            description = "Provides functionality to add a new Code Set Dates and the Criteria associated with it. "
                    + "Security Restrictions: To create: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody CodeSetDate createCodeSetDate(@RequestBody(required = true) CodeSetDate codeSetDate) throws EntityRetrievalException {
        return codeSetDateManager.create(codeSetDate);
    }

    @Operation(summary = "Update a Code Set Dates.",
            description = "Provides functionality to update a Code Set Dates and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody CodeSetDate updateCodeSetDate(@RequestBody(required = true) CodeSetDate codeSetDate) throws EntityRetrievalException {
        return codeSetDateManager.update(codeSetDate);
    }

    @Operation(summary = "Delete a Code Set Dates.",
            description = "Provides functionality to delete an existing Code Set Dates and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{codeSetDateId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("code_setDateId") Long codeSetDateId) throws EntityRetrievalException {
        codeSetDateManager.delete(codeSetDateId);
    }

}
