package gov.healthit.chpl.web.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.CognitoAuthenticationManager;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "cognito/users", description = "Allows management of Cognito users.")
@RestController
@RequestMapping("/cognito/users")
public class CognitoUserController {

    private CognitoAuthenticationManager cognitoAuthenticationManager;

    @Autowired
    public CognitoUserController(CognitoAuthenticationManager cognitoAuthenticationManager) {
        this.cognitoAuthenticationManager = cognitoAuthenticationManager;
    }

    @Operation(summary = "View a specific user's details.",
            description = "The logged in user must either be the user in the parameters, have ROLE_ADMIN, or "
                    + "have ROLE_ACB.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{email}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody User getUser(@PathVariable("email") String email) throws UserRetrievalException {
            return cognitoAuthenticationManager.getUserInfo(email);
    }
}
