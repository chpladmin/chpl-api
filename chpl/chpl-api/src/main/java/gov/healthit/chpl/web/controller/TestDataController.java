package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.testdata.TestData;
import gov.healthit.chpl.testdata.TestDataManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "test-data", description = "Endpoints related to Test Data.")
@RestController
@RequestMapping("/test-data")
public class TestDataController {
    private TestDataManager testDataManager;

    @Autowired
    public TestDataController(TestDataManager testDataManager) {
        this.testDataManager = testDataManager;
    }

    @Operation(summary = "Retrieve all current Test Data. ",
            description = "Returns all of the Test Data that is currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestData> getAllTestData() {
        return testDataManager.getAll();
    }

    @Operation(summary = "Get all criteria that Test Data can be associated with.",
            description = "Returns all of the Criteria that a Test Data can be associated to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForTestData() {
        return testDataManager.getCertificationCriteriaForTestData();
    }
}
