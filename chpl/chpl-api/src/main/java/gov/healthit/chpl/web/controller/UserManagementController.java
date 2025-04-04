package gov.healthit.chpl.web.controller;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.user.cognito.CognitoUserManager;
import gov.healthit.chpl.user.cognito.invitation.CognitoInvitationManager;
import gov.healthit.chpl.user.cognito.invitation.CognitoUserInvitation;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Tag(name = "users", description = "Allows management of users.")
@RestController
@RequestMapping("/users")
public class UserManagementController {
    private CognitoUserManager cognitoUserManager;
    private CognitoInvitationManager cognitoInvitationManager;
    private ErrorMessageUtil msgUtil;

    private long authorizationLengthInDays;

    @Autowired
    public UserManagementController(CognitoUserManager cognitoUserManager,
            CognitoInvitationManager cognitoInvitationManager,
            ErrorMessageUtil errorMessageUtil,
            @Value("${authorizationLengthInDays}") Long authorizationLengthInDays) {
        this.msgUtil = errorMessageUtil;
        this.cognitoUserManager = cognitoUserManager;
        this.cognitoInvitationManager = cognitoInvitationManager;
        this.authorizationLengthInDays = authorizationLengthInDays;
    }

    @Operation(summary = "Update the currently logged in user with an additional organization.",
            description = "Update the currently logged in user with an additional organization.  This"
                    + "is typically adding another developer or ONC-ACB to an existing user's list "
                    + "of organizations they have access to.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/authorize/{invitationToken}", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public User addOrganizationToUser(@PathVariable("invitationToken") UUID invitationToken, @RequestHeader("authorization") String jwt)
            throws UserRetrievalException, InvalidArgumentsException, ActivityException {

        return cognitoUserManager.addOrganizationToUser(invitationToken, jwt.split(" ")[1]);
    }

    @Operation(summary = "View a specific user's details.",
            description = "The logged in user must either be the user in the parameters, have role chpl-admin, or "
                    + "have role chpl-onc-acb.",
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
                    + "the following: 1) POST /users/invitation 2) POST /users or POST users/authorize/{invitationToken}. "
                    + "Security Restrictions: User must have either role chpl-admin or chpl-onc to invite users to "
                    + "any organization. Users with role chpl-onc-acb can add users to their own organization.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/invitation", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
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
            default:
                LOGGER.error("Invitation group name not handled: " + invitation.getGroupName());
        }
        return createdInvitiation;
    }

    @Operation(summary = "Create a new user account from an invitation.",
            description = "An individual who has been invited to the CHPL has a special user key in their invitation email. "
                    + "That user key along with all the information needed to create a new user's account "
                    + "can be passed in here. The account is created but cannot be used until that user "
                    + "confirms that their email address is valid. The correct order to call invitation requests is "
                    + "the following: 1) POST /users/invitation 2) POST /users or POST users/authorize/{invitationToken}",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public void addUser(@RequestBody CreateUserFromInvitationRequest userInfo) throws InvalidArgumentsException,
        ValidationException, EmailNotSentException, UserRetrievalException, UserCreationException, ActivityException {
        UUID token = null;

        try {
            token = UUID.fromString(userInfo.getHash());
        } catch (IllegalArgumentException ex) {
            LOGGER.error("Attempting to create a user from a invalid invitation token: " + userInfo.getHash(), ex);
            throw new InvalidArgumentsException(msgUtil.getMessage("user.invitation.invalid",
                    authorizationLengthInDays + "",
                    authorizationLengthInDays == 1 ? "" : "s"));
        }

        try {
            CognitoUserInvitation invitation = cognitoInvitationManager.getByToken(token);
            if (invitation != null) {
                cognitoUserManager.createUser(userInfo);
            } else {
                throw new InvalidArgumentsException(msgUtil.getMessage("user.invitation.invalid",
                        authorizationLengthInDays + "",
                        authorizationLengthInDays == 1 ? "" : "s"));
            }
        } catch (ValidationException ex) {
            throw ex;
        } catch (Exception ex) {
            LOGGER.error("Error creating user from invitation.", ex);
            throw new InvalidArgumentsException(msgUtil.getMessage("user.invitation.invalid",
                    authorizationLengthInDays + "",
                    authorizationLengthInDays == 1 ? "" : "s"));
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    @Operation(summary = "Modify user information.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{cognitoUserId:^[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}$}",
            method = RequestMethod.PUT,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public User updateUserDetails(@RequestBody User user, @PathVariable("cognitoUserId") UUID cognitoUserId)
            throws ValidationException, UserRetrievalException, ActivityException {
        if (!cognitoUserId.equals(user.getCognitoId())) {
            throw new ValidationException(msgUtil.getMessage("url.body.notMatch"));
        }
        return cognitoUserManager.updateUser(user);
    }

    @Operation(summary = "View users of the system.",
            description = "Security Restrictions: Users must have either role chpl-admin or chpl-onc to see all users. "
                    + "Users with role chpl-onc-acb or chpl-developer can see users in their own organizations. "
                    + "Users with role chpl-cms-staff can see themselves.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody UsersResponse getUsers() {
        List<User> users = cognitoUserManager.getAll();
        UsersResponse response = new UsersResponse();
        response.setUsers(users);
        return response;
    }
}
