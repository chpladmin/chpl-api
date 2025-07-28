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
import gov.healthit.chpl.conformanceMethod.ConformanceMethodManager;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "conformance-methods", description = "Allows management of Conformance Methods.")
@RestController
@RequestMapping("/conformance-methods")
public class ConformanceMethodController {

    private ConformanceMethodManager cmManager;

    @Autowired
    public ConformanceMethodController(ConformanceMethodManager cmManager) {
        this.cmManager = cmManager;
    }

    @Operation(summary = "Retrieve all current Conformance Methods. ",
            description = "Returns all of the Conformance Methods that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<ConformanceMethod> getAll() {
        return cmManager.getAll();
    }

    @Operation(summary = "Get all criteria that a Conformance Method can be associated with.",
            description = "Returns all of the Criteria that a Conformance Method can be associated with.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForConformanceMethods() {
        return cmManager.getCertificationCriteriaForConformanceMethods();
    }

    @Operation(summary = "Create a Conformance Method.",
            description = "Provides functionality to add a new Conformance Method associated with criteria. "
                    + "Security Restrictions: User must have role chpl-admin or chpl-onc",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ConformanceMethod createConformanceMethod(@RequestBody(required = true) ConformanceMethod conformanceMethod)
            throws EntityRetrievalException, ValidationException {
        return cmManager.create(conformanceMethod);
    }

    @Operation(summary = "Update a Conformance Method.",
            description = "Provides functionality to update a Conformance Method and the Criteria associated with it. "
                    + "Security Restrictions: User must have role chpl-admin or chpl-onc",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody ConformanceMethod updateConformanceMethod(@RequestBody(required = true) ConformanceMethod conformanceMethod)
            throws EntityRetrievalException, ValidationException {
        return cmManager.update(conformanceMethod);
    }

    @Operation(summary = "Delete a Conformance Method.",
            description = "Provides functionality to delete an existing Conformance Method and the Criteria associated with it. "
                    + "Security Restrictions: User must have role chpl-admin or chpl-onc",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{conformanceMethodId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("conformanceMethodId") Long conformanceMethodId) throws EntityRetrievalException, ValidationException {
        cmManager.delete(conformanceMethodId);
    }

}
