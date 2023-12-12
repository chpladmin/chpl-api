package gov.healthit.chpl.web.controller;

import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.FeatureList;
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
    private FF4j ff4j;

    @Autowired
    public CognitoUserController(CognitoAuthenticationManager cognitoAuthenticationManager, FF4j ff4j) {
        this.cognitoAuthenticationManager = cognitoAuthenticationManager;
        this.ff4j = ff4j;
    }

    @Operation(summary = "View a specific user's details.",
            description = "The logged in user must either be the user in the parameters, have ROLE_ADMIN, or "
                    + "have ROLE_ACB.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{ssoUserId}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody User getUser(@PathVariable("ssoUserId") UUID ssoUserId) throws UserRetrievalException {
        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This feature has not been implemented");
        }
            return cognitoAuthenticationManager.getUserInfo(ssoUserId);
    }
}
