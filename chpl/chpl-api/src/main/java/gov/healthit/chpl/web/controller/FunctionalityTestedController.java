package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.functionalityTested.FunctionalityTested;
import gov.healthit.chpl.functionalityTested.FunctionalityTestedManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedApiResponseFields;
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

    @DeprecatedApiResponseFields(friendlyUrl = "/functionalities-tested",
            responseClass = FunctionalityTested.class)
    @Operation(summary = "Retrieve all Functionalities Tested. ",
            description = "Returns all of the Functionalities Tested that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<FunctionalityTested> getAllFunctionalitiesTested() {
        return functionalityTestedManager.getFunctionalitiesTested();
    }
}
