package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.conformanceMethod.ConformanceMethodManager;
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodWithCriteria;
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
    public @ResponseBody List<ConformanceMethodWithCriteria> getAll() {
        return cmManager.getAll();
    }
}
