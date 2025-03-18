package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.cqm.CQMCriterionAllVersions;
import gov.healthit.chpl.cqm.CqmManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "cqms", description = "Endpoints related to Clinical Quality Measures.")
@RestController
@RequestMapping("/cqms")
public class CqmController {
    private CqmManager cqmManager;

    @Autowired
    public CqmController(CqmManager cqmManager) {
        this.cqmManager = cqmManager;
    }

    @Operation(summary = "Retrieve all Clinical Quality Measures. ",
            description = "Returns all of the Clinical Quality Measures that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CQMCriterionAllVersions> getAllCqms() {
        return cqmManager.getAllCqms();
    }
}
