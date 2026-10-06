package gov.healthit.chpl.web.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.targeteduser.TargetedUser;
import gov.healthit.chpl.targeteduser.TargetedUserManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "targeted-users", description = "Endpoints related to Targeted Users.")
@RestController
@RequestMapping("/targeted-users")
public class TargetedUserController {
    private TargetedUserManager targetedUserManager;

    @Autowired
    public TargetedUserController(TargetedUserManager targetedUserManager) {
        this.targetedUserManager = targetedUserManager;
    }

    @Operation(summary = "Retrieve all current Targeted Users. ",
            description = "Returns all of the Targeted Users that are currently in the CHPL.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public @ResponseBody List<TargetedUser> getAllTargetedUsers() {
        return targetedUserManager.getAll();
    }
}
