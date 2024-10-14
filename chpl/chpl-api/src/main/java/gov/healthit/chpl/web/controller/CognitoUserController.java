package gov.healthit.chpl.web.controller;

import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.ChplAccountStatusException;
import gov.healthit.chpl.domain.CognitoRefreshTokenRequest;
import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.auth.CognitoForgotPasswordRequest;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.CognitoLogoutRequest;
import gov.healthit.chpl.domain.auth.CognitoNewPasswordRequiredRequest;
import gov.healthit.chpl.domain.auth.CognitoSetForgottenPasswordRequest;
import gov.healthit.chpl.domain.auth.CognitoUpdatePasswordRequest;
import gov.healthit.chpl.domain.auth.LoginCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.user.cognito.CognitoUserManager;
import gov.healthit.chpl.user.cognito.authentication.CognitoAuthenticationChallengeException;
import gov.healthit.chpl.user.cognito.authentication.CognitoAuthenticationManager;
import gov.healthit.chpl.user.cognito.authentication.CognitoAuthenticationResponse;
import gov.healthit.chpl.user.cognito.authentication.CognitoPasswordResetRequiredException;
import gov.healthit.chpl.user.cognito.invitation.CognitoInvitationManager;
import gov.healthit.chpl.user.cognito.invitation.CognitoUserInvitation;
import gov.healthit.chpl.user.cognito.password.CognitoPasswordManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "cognito/users", description = "Allows management of Cognito users.")
@RestController
@RequestMapping("/cognito/users")
public class CognitoUserController {

    private CognitoUserManager cognitoUserManager;
    private CognitoPasswordManager cognitoPasswordManager;
    private CognitoInvitationManager cognitoInvitationManager;
    private CognitoAuthenticationManager cognitoAuthenticationManager;
    private ErrorMessageUtil errorMessageUtil;
    private FF4j ff4j;

    @Autowired
    public CognitoUserController(CognitoUserManager cognitoUserManager, CognitoPasswordManager cognitoPasswordManager,
            CognitoAuthenticationManager cognitoAuthenticationManager, CognitoInvitationManager cognitoInvitationManager,
            ErrorMessageUtil errorMessageUtil, FF4j ff4j) {

        this.cognitoUserManager = cognitoUserManager;
        this.cognitoPasswordManager = cognitoPasswordManager;
        this.cognitoAuthenticationManager = cognitoAuthenticationManager;
        this.cognitoInvitationManager = cognitoInvitationManager;
        this.errorMessageUtil = errorMessageUtil;
        this.ff4j = ff4j;
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
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public CognitoAuthenticationResponse authenticateJSON(@RequestBody LoginCredentials credentials) throws CognitoAuthenticationChallengeException, CognitoPasswordResetRequiredException {

        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This method has not been implemented");
        }

        CognitoAuthenticationResponse response = cognitoAuthenticationManager.authenticate(credentials);
        if (response == null) {
            throw new ChplAccountStatusException(errorMessageUtil.getMessage("auth.loginNotAllowed"));
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
    @RequestMapping(value = "/authenticate/challenge/new-password-required", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public CognitoAuthenticationResponse newPasswordRequiredChallenge(@RequestBody CognitoNewPasswordRequiredRequest request) throws ValidationException {

        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This method has not been implemented");
        }

        CognitoAuthenticationResponse response = cognitoAuthenticationManager.newPassworRequiredChallenge(request);
        if (response == null) {
            throw new ChplAccountStatusException(errorMessageUtil.getMessage("auth.loginNotAllowed"));
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

        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This method has not been implemented");
        }

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
        if (!ff4j.check(FeatureList.SSO)) {
            throw new NotImplementedException("This method has not been implemented");
        }

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

    @Operation(summary = "View a specific user's details.",
            description = "The logged in user must either be the user in the parameters, have ROLE_ADMIN, or "
                    + "have ROLE_ACB.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{cognitoUserId}", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody User getUser(@PathVariable("cognitoUserId") UUID cognitoUserId) throws UserRetrievalException {
        return cognitoUserManager.getUserInfo(cognitoUserId);
    }

    @Operation(summary = "Invite a user to the CHPL.",
            description = "This request creates an invitation that is sent to the email address provided. "
                    + "The recipient of this invitation can then choose to create a new account "
                    + "or add the permissions contained within the invitation to an existing account "
                    + "if they have one. Said another way, an invitation can be used to create or "
                    + "modify CHPL user accounts." + "The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize. "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC can invite users to any organization.  "
                    + "ROLE_ACB can add users to their own organization.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/invite", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public CognitoUserInvitation inviteUser(@RequestBody CognitoUserInvitation invitation)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException, ValidationException {
        invitation.setEmail(StringUtils.normalizeSpace(invitation.getEmail()));

        CognitoUserInvitation createdInvitiation = null;
        switch (invitation.getGroupName()) {
            case CognitoGroups.CHPL_ADMIN:
                createdInvitiation = cognitoInvitationManager.inviteAdminUser(invitation);
                break;
            case CognitoGroups.CHPL_ONC:
                createdInvitiation = cognitoInvitationManager.inviteOncUser(invitation);
                break;
            case CognitoGroups.CHPL_ACB:
                createdInvitiation = cognitoInvitationManager.inviteOncAcbUser(invitation);
                break;
            case CognitoGroups.CHPL_DEVELOPER:
                createdInvitiation = cognitoInvitationManager.inviteDeveloperUser(invitation);
                break;
            case CognitoGroups.CHPL_CMS_STAFF:
                createdInvitiation = cognitoInvitationManager.inviteCmsUser(invitation);
                break;
        }
        return createdInvitiation;
    }

    @Operation(summary = "Create a new user account from an invitation.",
            description = "An individual who has been invited to the CHPL has a special user key in their invitation email. "
                    + "That user key along with all the information needed to create a new user's account "
                    + "can be passed in here. The account is created but cannot be used until that user "
                    + "confirms that their email address is valid. The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public void createUser(@RequestBody CreateUserFromInvitationRequest userInfo) throws ValidationException, EmailNotSentException, UserCreationException {

        try {
            //This should set the security context to user "invited user" role
            Authentication authenticator = AuthUtil.getInvitedUserAuthenticator(null);
            SecurityContextHolder.getContext().setAuthentication(authenticator);
            CognitoUserInvitation invitation = cognitoInvitationManager.getByToken(UUID.fromString(userInfo.getHash()));
            if (invitation != null) {
                cognitoUserManager.createUser(userInfo);
            }
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    @RequestMapping(value = "/refresh-token", method = RequestMethod.POST,
            produces = "application/json; charset=utf-8")
    public CognitoAuthenticationResponse refreshToken(@RequestBody CognitoRefreshTokenRequest request) {
        return cognitoAuthenticationManager.refreshAuthenticationTokens(request.getRefreshToken(), UUID.fromString(request.getCognitoId()));
    }

    @Operation(summary = "Modify user information.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{cognitoUserId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public User updateUserDetails(@RequestBody User userInfo, @PathVariable("cognitoUserId") UUID cognitoUserId) throws ValidationException, UserRetrievalException {
        if (!cognitoUserId.equals(userInfo.getCognitoId())) {
            throw new ValidationException(errorMessageUtil.getMessage("url.body.notMatch"));
        }
        return cognitoUserManager.updateUser(userInfo);
    }
}
