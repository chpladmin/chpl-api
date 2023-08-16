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

import gov.healthit.chpl.criteriaattribute.functionalitytested.FunctionalityTested;
import gov.healthit.chpl.criteriaattribute.functionalitytested.FunctionalityTestedManager;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "functionality-tested", description = "Endpoints related to functionalities tested.")
@RestController
@RequestMapping("/functionalities-tested")
public class FunctionalityTestedController {

    private FunctionalityTestedManager functionalityTestedManager;

    @Autowired
    public FunctionalityTestedController(FunctionalityTestedManager functionalityTestedManager) {
        this.functionalityTestedManager = functionalityTestedManager;
    }

    @Operation(summary = "Retrieve all Functionalities Tested. ",
            description = "Returns all of the Functionalities Tested that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<FunctionalityTested> getAllFunctionalitiesTested() {
        return functionalityTestedManager.getAll();
    }

    @Operation(summary = "Get all criteria that Functionality Tested can be associated with.",
            description = "Returns all of the Criteria that a Functionality Tested can be associated to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForFunctionalitiesTested() {
        return functionalityTestedManager.getCertificationCriteriaForFunctionalitiesTested();
    }

    @Operation(summary = "Create a Functionality Tested.",
            description = "Provides functionality to add a new Functionality Tested and the Criteria associated with it. "
                    + "Security Restrictions: To create: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody FunctionalityTested createFunctionalityTested(@RequestBody(required = true) FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        return functionalityTestedManager.create(functionalityTested);
    }

    @Operation(summary = "Update a Functionality Tested.",
            description = "Provides functionality to update a Functionality Tested and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody FunctionalityTested updateFunctionalityTested(@RequestBody(required = true) FunctionalityTested functionalityTested) throws EntityRetrievalException, ValidationException {
        return functionalityTestedManager.update(functionalityTested);
    }

    @Operation(summary = "Delete a Functionality Tested.",
            description = "Provides functionality to delete an existing Functionality Tested and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{functionalityTestedId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("functionalityTestedId") Long functionalityTestedId) throws EntityRetrievalException, ValidationException {
        functionalityTestedManager.delete(functionalityTestedId);
    }

}
