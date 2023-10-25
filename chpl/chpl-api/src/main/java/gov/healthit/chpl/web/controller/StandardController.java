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
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.standard.Standard;
import gov.healthit.chpl.standard.StandardManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "standard", description = "Endpoints related to Standards.")
@RestController
@RequestMapping("/standard")
public class StandardController {
    private StandardManager standardManager;

    @Autowired
    public StandardController(StandardManager standardManager) {
        this.standardManager = standardManager;
    }

    @DeprecatedApiResponseFields(friendlyUrl = "/standard",
            responseClass = Standard.class)
    @Operation(summary = "Retrieve all Standards. ",
            description = "Returns all of the Standards that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<Standard> getAllStandards() {
        return standardManager.getAll();
    }

    @Operation(summary = "Get all criteria that Standard can be associated with.",
            description = "Returns all of the Criteria that a Standard can be associated to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForStandards() {
        return standardManager.getCertificationCriteriaForStandards();
    }

    @Operation(summary = "Create a Standard.",
            description = "Provides functionality to add a new Standard and the Criteria associated with it. "
                    + "Security Restrictions: To create: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Standard createStandard(@RequestBody(required = true) Standard standard) throws EntityRetrievalException, ValidationException {
        return standardManager.create(standard);
    }

    @Operation(summary = "Update a Standard.",
            description = "Provides functionality to update a Standard and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody Standard updateStandard(@RequestBody(required = true) Standard standard) throws EntityRetrievalException, ValidationException {
        return standardManager.update(standard);
    }

    @Operation(summary = "Delete a Standard.",
            description = "Provides functionality to delete an existing Standard and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{standardId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("standardId") Long stnadardId) throws EntityRetrievalException, ValidationException {
        standardManager.delete(stnadardId);
    }

}
