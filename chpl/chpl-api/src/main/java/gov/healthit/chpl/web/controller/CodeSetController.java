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
import gov.healthit.chpl.codeset.CodeSet;
import gov.healthit.chpl.codeset.CodeSetManager;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "code-sets", description = "Endpoints related to Code Sets.")
@RestController
@RequestMapping("/code-sets")
public class CodeSetController {
    private CodeSetManager codeSetManager;

    @Autowired
    public CodeSetController(CodeSetManager codeSetManager) {
        this.codeSetManager = codeSetManager;
    }

    @Operation(summary = "Retrieve all Code Set Dates. ",
            description = "Returns all of the Code Set Dates that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CodeSet> getAllCodeSets() {
        return codeSetManager.getAll();
    }

    @Operation(summary = "Get all criteria that a Code Set Date can be associated with.",
            description = "Returns all of the Criteria that a Code Set Date can be associated to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForCodeSets() {
        return codeSetManager.getCertificationCriteriaForCodeSets();
    }

    @Operation(summary = "Create a Code Set Date.",
            description = "Provides functionality to add a new Code Set Date and the Criteria associated with it. "
                    + "Security Restrictions: To create: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody CodeSet createCodeSet(@RequestBody(required = true) CodeSet codeSet) throws EntityRetrievalException {
        return codeSetManager.create(codeSet);
    }

    @Operation(summary = "Update a Code Set Date.",
            description = "Provides functionality to update a Code Set Date and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody CodeSet updateCodeSet(@RequestBody(required = true) CodeSet codeSet) throws EntityRetrievalException {
        return codeSetManager.update(codeSet);
    }

    @Operation(summary = "Delete a Code Set Date.",
            description = "Provides functionality to delete an existing Code Set Date and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{codeSetId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("codeSetId") Long codeSetId) throws EntityRetrievalException {
        codeSetManager.delete(codeSetId);
    }

}
