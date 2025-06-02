package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.testprocedure.TestProcedure;
import gov.healthit.chpl.testprocedure.TestProcedureManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "test-procedures", description = "Endpoints related to Test Procedures.")
@RestController
@RequestMapping("/test-procedures")
public class TestProcedureController {
    private TestProcedureManager testProcedureManager;

    @Autowired
    public TestProcedureController(TestProcedureManager testProcedureManager) {
        this.testProcedureManager = testProcedureManager;
    }

    @Operation(summary = "Retrieve all current Test Procedures. ",
            description = "Returns all of the Test Procedures that is currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestProcedure> getAllTestProcedures() {
        return testProcedureManager.getAll();
    }

    @Operation(summary = "Get all criteria that Test Procedures can be associated with.",
            description = "Returns all of the Criteria that a Test Procedure can be associated to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForTestProcedure() {
        return testProcedureManager.getCertificationCriteriaForTestProcedures();
    }
}
