package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.teststandard.TestStandard;
import gov.healthit.chpl.teststandard.TestStandardManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "test-standards", description = "Endpoints related to Test Standards.")
@RestController
@RequestMapping("/test-standards")
public class TestStandardController {
    private TestStandardManager testStandardManager;

    @Autowired
    public TestStandardController(TestStandardManager testStandardManager) {
        this.testStandardManager = testStandardManager;
    }

    @Operation(summary = "Retrieve all current Test Standards. ",
            description = "Returns all of the Test Standards that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestStandard> getAllTestStandards() {
        return testStandardManager.getAll();
    }

    @Operation(summary = "Get all criteria that Test Standards can be associated with.",
            description = "Returns all of the Criteria that a Test Standard can be associated to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForTestStandards() {
        return testStandardManager.getCertificationCriteriaForTestStandards();
    }
}
