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

import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.criteriaattribute.testtool.TestToolManager;
import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "test-tools", description = "Allows management of Test Tools.")
@RestController
@RequestMapping("/test-tools")
public class TestToolController {

    private TestToolManager testToolManager;

    @Autowired
    public TestToolController(TestToolManager testToolManager) {
        this.testToolManager = testToolManager;
    }

    @Operation(summary = "Retrieve all current Test Tools. ",
            description = "Returns all of the Test Tools that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestTool> getAllTestTools() {
        return testToolManager.getAll();
    }

    @Operation(summary = "Get all criteria that Test Tools can be associated with.",
            description = "Returns all of the Criteria that a Test Tool can be associated to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/criteria", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<CertificationCriterion> getCertificationCriteriaForSvap() {
        return testToolManager.getCertificationCriteriaForTestTools();
    }

    @Operation(summary = "Create a Test Tool.",
            description = "Provides functionality to add a new Test Tool and the Criteria associated with it. "
                    + "Security Restrictions: To create: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody TestTool createTestTool(@RequestBody(required = true) TestTool testTool) throws EntityRetrievalException, ValidationException {
        return testToolManager.create(testTool);
    }

    @Operation(summary = "Update a Test Tool.",
            description = "Provides functionality to update a Test Tool and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody TestTool updateTestTool(@RequestBody(required = true) TestTool testTool) throws EntityRetrievalException, ValidationException {
        return testToolManager.update(testTool);
    }

    @Operation(summary = "Delete a Test Tool.",
            description = "Provides functionality to delete an existing Test Tool and the Criteria associated with it. "
                    + "Security Restrictions: To update: ROLE_ADMIN or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{testToolId}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public void delete(@PathVariable("testToolId") Long testToolId) throws EntityRetrievalException, ValidationException {
        testToolManager.delete(testToolId);
    }

}
