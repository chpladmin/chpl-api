package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.criteriaattribute.rule.Rule;
import gov.healthit.chpl.criteriaattribute.rule.RuleManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "rules", description = "Allows retrieval of existing Rules.")
@RestController
@RequestMapping("/rules")
public class RuleController {
    private RuleManager ruleManager;

    @Autowired
    public RuleController(RuleManager ruleManager) {
        this.ruleManager = ruleManager;
    }

    @Operation(summary = "Retrieve all current Rules. ",
            description = "Returns all of the Rules that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<Rule> getAllTestTools() {
        return ruleManager.getAll();
    }

}
