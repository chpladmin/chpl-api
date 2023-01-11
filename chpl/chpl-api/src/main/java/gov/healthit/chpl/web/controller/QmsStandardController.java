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

import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.qmsStandard.QmsStandard;
import gov.healthit.chpl.qmsStandard.QmsStandardManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "qms-standards", description = "Allows management of QMS Standards.")
@RestController
@RequestMapping("/qms-standards")
public class QmsStandardController {

    private QmsStandardManager qmsStandardManager;

    @Autowired
    public QmsStandardController(QmsStandardManager qmsStandardManager) {
        this.qmsStandardManager = qmsStandardManager;
    }

    @Operation(summary = "Update a QMS Standard.",
            description = "Provides functionality to update the name of a QMS Standard. "
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody QmsStandard updateQmsStandard(
            @RequestBody(required = true) QmsStandard qmsStandard) throws EntityRetrievalException, ValidationException {
        return qmsStandardManager.update(qmsStandard);
    }

    @Operation(summary = "Create a QMS Standard.",
            description = "Provides functionality to add a new QMS Standard. "
                    + "Security Restrictions: To create: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody QmsStandard createQmsStandard(
            @RequestBody(required = true) QmsStandard qmsStandard) throws EntityCreationException, ValidationException {
        return qmsStandardManager.create(qmsStandard);
    }

    @Operation(summary = "Delete a QMS Standard.",
            description = "Provides functionality to delete an existing QMS Standard. "
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{qmsStandardId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("qmsStandardId") Long qmsStandardId) throws EntityRetrievalException, ValidationException {
        qmsStandardManager.delete(qmsStandardId);
    }

    @Operation(summary = "Retrieve all current QMS Standards. ",
            description = "Returns all of the QMS Standards that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<QmsStandard> getAllQmsStandards() {
        return qmsStandardManager.getAllQmsStandards();
    }
}
