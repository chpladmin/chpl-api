package gov.healthit.chpl.web.controller;

import java.util.UUID;

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

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.user.cognito.CognitoUserInvitation;
import gov.healthit.chpl.user.cognito.CognitoUserManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "cognito/users", description = "Allows management of Cognito users.")
@RestController
@RequestMapping("/cognito/users")
public class CognitoUserController {

    private CognitoUserManager cognitoUserManager;


    @Autowired
    public CognitoUserController(CognitoUserManager cognitoUserManager) {
        this.cognitoUserManager = cognitoUserManager;
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
        return cognitoUserManager.getUserInfo(ssoUserId);
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

        CognitoUserInvitation createdInvitiation = null;
        switch (invitation.getGroupName()) {
            case "chpl-admin":
                createdInvitiation = cognitoUserManager.inviteAdminUser(invitation);
                break;
            case "chpl-onc":
                createdInvitiation = cognitoUserManager.inviteOncUser(invitation);
                break;
            case "chpl-onc-acb":
                createdInvitiation = cognitoUserManager.inviteOncAcbUser(invitation);
                break;
            case "chpl-developer":
                createdInvitiation = cognitoUserManager.inviteOncUser(invitation);
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
            CognitoUserInvitation invitation = cognitoUserManager.getInvitation(UUID.fromString(userInfo.getHash()));
            if (invitation != null) {
                cognitoUserManager.createUser(userInfo);
            }
        } finally {
        SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

}
