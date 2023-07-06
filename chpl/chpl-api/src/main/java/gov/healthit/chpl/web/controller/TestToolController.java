package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.criteriaattribute.testtool.TestTool;
import gov.healthit.chpl.criteriaattribute.testtool.TestToolManager;
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
            description = "Returns all of the Test Tools that are currenty in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TestTool> getAllTestTools() {
        return testToolManager.getAll();
    }
}
