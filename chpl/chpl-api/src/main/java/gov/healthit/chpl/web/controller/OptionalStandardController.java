package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.optionalStandard.OptionalStandardManager;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "optional-standards", description = "Allows management of Optional Standards.")
@RestController
@RequestMapping("/optional-standards")
public class OptionalStandardController {

    private OptionalStandardManager osManager;

    @Autowired
    public OptionalStandardController(OptionalStandardManager osManager) {
        this.osManager = osManager;
    }

    @Operation(summary = "Retrieve all current Optional Standards. ",
            description = "Returns all of the Optional Standards that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<OptionalStandard> getAll() {
        return osManager.getAll();
    }
}
