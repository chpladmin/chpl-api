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

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandard;
import gov.healthit.chpl.accessibilityStandard.AccessibilityStandardManager;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "accessibility-standards", description = "Allows management of Accessibility Standards.")
@RestController
@RequestMapping("/accessibility-standards")
public class AccessibilityStandardController {

    private AccessibilityStandardManager accessibilityStandardManager;

    @Autowired
    public AccessibilityStandardController(AccessibilityStandardManager accessibilityStandardManager) {
        this.accessibilityStandardManager = accessibilityStandardManager;
    }

    @Operation(summary = "Update an Accessibility Standard.",
            description = "Provides functionality to update the name of an Accessibility Standard. "
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody AccessibilityStandard updateAccessibilityStandard(
            @RequestBody(required = true) AccessibilityStandard accessibilityStandard) throws EntityRetrievalException, ValidationException {
        return accessibilityStandardManager.update(accessibilityStandard);
    }

    @Operation(summary = "Create an Accessibility Standard.",
            description = "Provides functionality to add a new Accessibility Standard. "
                    + "Security Restrictions: To create: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody AccessibilityStandard createAccessibilityStandard(
            @RequestBody(required = true) AccessibilityStandard accessibilityStandard) throws EntityCreationException, ValidationException {
        return accessibilityStandardManager.create(accessibilityStandard);
    }

    @Operation(summary = "Delete an Accessibility Standard.",
            description = "Provides functionality to delete an existing Accessibility Standard. "
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{accessibilityStandardId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("accessibilityStandardId") Long accessibilityStandardId) throws EntityRetrievalException, ValidationException {
        accessibilityStandardManager.delete(accessibilityStandardId);
    }

    @Operation(summary = "Retrieve all current Accessibility Standards. ",
            description = "Returns all of the Accessibility Standards that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<AccessibilityStandard> getAllAccessibilityStandards() {
        return accessibilityStandardManager.getAllAccessibilityStandards();
    }
}
