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
import gov.healthit.chpl.ucdProcess.UcdProcess;
import gov.healthit.chpl.ucdProcess.UcdProcessManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "ucd-processes", description = "Allows management of user-centered design (UCD) Processes.")
@RestController
@RequestMapping("/ucd-processes")
public class UcdProcessController {

    private UcdProcessManager ucdProcessManager;

    @Autowired
    public UcdProcessController(UcdProcessManager ucdProcessManager) {
        this.ucdProcessManager = ucdProcessManager;
    }

    @Operation(summary = "Update a User-Centered Design Process.",
            description = "Provides functionality to update the name of a UCD Process. "
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody UcdProcess updateUcdProcess(@RequestBody(required = true) UcdProcess ucdProcess) throws EntityRetrievalException, ValidationException {
        return ucdProcessManager.update(ucdProcess);
    }

    @Operation(summary = "Create a User-Centered Design PRocess.",
            description = "Provides functionality to add a new UCD Process. "
                    + "Security Restrictions: To create: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody UcdProcess createUcdProcess(@RequestBody(required = true) UcdProcess ucdProcess) throws EntityCreationException, ValidationException {
        return ucdProcessManager.create(ucdProcess);
    }

    @Operation(summary = "Delete a User-Centered Design PRocess.",
            description = "Provides functionality to delete an existing UCD Process. "
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{ucdProcessId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("ucdProcessId") Long ucdProcessId) throws EntityRetrievalException, ValidationException {
        ucdProcessManager.delete(ucdProcessId);
    }

    @Operation(summary = "Retrieve all current User-Centered Design Processes. ",
            description = "Returns all of the UCD Processes that are currenty in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<UcdProcess> getAllUcdProcesses() {
        return ucdProcessManager.getAllUcdProcesses();
    }
}
