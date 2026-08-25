package gov.healthit.chpl.web.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.auth.ChplAccountStatusException;
import gov.healthit.chpl.domain.CognitoRefreshTokenRequest;
import gov.healthit.chpl.domain.auth.CognitoForgotPasswordRequest;
import gov.healthit.chpl.domain.auth.CognitoLogoutRequest;
import gov.healthit.chpl.domain.auth.CognitoNewPasswordRequiredRequest;
import gov.healthit.chpl.domain.auth.CognitoSetForgottenPasswordRequest;
import gov.healthit.chpl.domain.auth.CognitoUpdatePasswordRequest;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.user.cognito.authentication.CognitoAuthenticationChallengeException;
import gov.healthit.chpl.user.cognito.authentication.CognitoAuthenticationManager;
import gov.healthit.chpl.user.cognito.authentication.CognitoAuthenticationResponse;
import gov.healthit.chpl.user.cognito.authentication.CognitoPasswordResetRequiredException;
import gov.healthit.chpl.user.cognito.password.CognitoPasswordManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "auth", description = "User authentication operations including login.")
@RestController
@RequestMapping("/auth")
@Log4j2
public class AuthenticationController {
    private CognitoAuthenticationManager cognitoAuthenticationManager;
    private CognitoPasswordManager cognitoPasswordManager;
    private ErrorMessageUtil msgUtil;


    @Autowired
    public AuthenticationController(CognitoAuthenticationManager cognitoAuthenticationManager,
            CognitoPasswordManager cognitoPasswordManager,
            ErrorMessageUtil msgUtil) {
        this.cognitoAuthenticationManager = cognitoAuthenticationManager;
        this.cognitoPasswordManager = cognitoPasswordManager;
        this.msgUtil = msgUtil;
    }


    @Operation(summary = "Log in.",
            description = "Call this method to authenticate a user. The value returned is that user's "
                    + "token which must be passed on all subsequent requests in the Authorization header. "
                    + "Specifically, the Authorization header must have a value of 'Bearer token-that-gets-returned'.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            }
        )
    @ApiResponse(responseCode = "470", description = "The user is required to respond to the described challenge.")
    @RequestMapping(value = "", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public CognitoAuthenticationResponse authenticate(@RequestBody LoginCredentials credentials) throws CognitoAuthenticationChallengeException, CognitoPasswordResetRequiredException {
        CognitoAuthenticationResponse response = cognitoAuthenticationManager.authenticate(credentials);
        if (response == null) {
            throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
        }
        return response;
    }

    @Operation(summary = "Log user out.",
            description = "Invalidates all of the tokens associated with the user.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            }
        )
    @RequestMapping(value = "/logout", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public void logout(@RequestBody CognitoLogoutRequest request) {
        cognitoAuthenticationManager.invalidateTokensForUser(request.getEmail());
    }


    @Operation(summary = "Set user's password in response to NEW_PASSWORD_REQUIRED challenge.",
            description = "Set user's password in response to NEW_PASSWORD_REQUIRED challenge.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            }
        )
    @RequestMapping(value = "/challenge/new-password-required", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public CognitoAuthenticationResponse newPasswordRequiredChallenge(@RequestBody CognitoNewPasswordRequiredRequest request) throws ValidationException {
        CognitoAuthenticationResponse response = cognitoAuthenticationManager.newPassworRequiredChallenge(request);
        if (response == null) {
            throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
        }
        return response;
    }

    @Operation(summary = "Start forgot password workflow",
            description = "Send a user an email with a link to reset their password.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            }
        )
    @RequestMapping(value = "/forgot-password/send-email", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public void requestEmailForForgottenPassword(@RequestBody CognitoForgotPasswordRequest request) throws EmailNotSentException {
        cognitoPasswordManager.sendForgotPasswordEmail(request.getUserName());
    }

    @Operation(summary = "Complete forgot password workflow",
            description = "Set user's password after requesting forgot password.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            }
        )
    @RequestMapping(value = "/forgot-password/set-password", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public void setForgottenPassword(@RequestBody CognitoSetForgottenPasswordRequest request) throws EmailNotSentException, ValidationException {
        cognitoPasswordManager.setForgottenPassword(request.getForgotPasswordToken(), request.getPassword());
    }

    @Operation(summary = "Update the password for the currently logged in user.",
            description = "Update the password for the currently logged in user.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            }
        )
    @RequestMapping(value = "/password", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public void setPassword(@RequestBody CognitoUpdatePasswordRequest request) throws EmailNotSentException, ValidationException, UserRetrievalException {
        cognitoPasswordManager.setPassword(request.getPassword(), request.getConfirmPassword());
    }

    @RequestMapping(value = "/refresh-token", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public CognitoAuthenticationResponse refreshToken(@RequestBody CognitoRefreshTokenRequest request)
            throws UserRetrievalException, CognitoAuthenticationChallengeException {
        return cognitoAuthenticationManager.refreshAuthenticationTokens(request.getRefreshToken(), UUID.fromString(request.getCognitoId()));
    }
}
