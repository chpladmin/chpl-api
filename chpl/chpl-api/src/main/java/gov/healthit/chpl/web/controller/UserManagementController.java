package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.ChplAccountEmailNotConfirmedException;
import gov.healthit.chpl.auth.ChplAccountStatusException;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.AuthorizeCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserInvitationDTO;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserAccountExistsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.InvitationManager;
import gov.healthit.chpl.manager.auth.AuthenticationManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;

@Tag(name = "users", description = "Allows management of users.")
@RestController
@RequestMapping("/users")
public class UserManagementController {
    private UserManager userManager;
    private InvitationManager invitationManager;
    private AuthenticationManager authenticationManager;
    private ErrorMessageUtil msgUtil;

    private long invitationLengthInDays;
    private long confirmationLengthInDays;
    private long authorizationLengthInDays;

    @Autowired
    public UserManagementController(UserManager userManager, InvitationManager invitationManager,
            AuthenticationManager authenticationManager,
            ErrorMessageUtil errorMessageUtil,
            @Value("${invitationLengthInDays}") Long invitationLengthDays,
            @Value("${confirmationLengthInDays}") Long confirmationLengthDays,
            @Value("${authorizationLengthInDays}") Long authorizationLengthInDays) {
        this.userManager = userManager;
        this.invitationManager = invitationManager;
        this.authenticationManager = authenticationManager;
        this.msgUtil = errorMessageUtil;

        this.invitationLengthInDays = invitationLengthDays;
        this.confirmationLengthInDays = confirmationLengthDays;
        this.authorizationLengthInDays = authorizationLengthInDays;
    }

    @Operation(summary = "Create a new user account from an invitation.",
            description = "An individual who has been invited to the CHPL has a special user key in their invitation email. "
                    + "That user key along with all the information needed to create a new user's account "
                    + "can be passed in here. The account is created but cannot be used until that user "
                    + "confirms that their email address is valid. The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize 3) /confirm ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public @ResponseBody User createUser(@RequestBody CreateUserFromInvitationRequest userInfo)
            throws ValidationException, EntityRetrievalException, InvalidArgumentsException,
            UserRetrievalException, MultipleUserAccountsException, UserCreationException,
            EmailNotSentException, JsonProcessingException, EntityCreationException {

        if (userInfo.getUser() == null || userInfo.getUser().getEmail() == null) {
            throw new ValidationException(msgUtil.getMessage("user.email.required"));
        }

        Set<String> errors = validateCreateUserFromInvitationRequest(userInfo);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }

        UserInvitation invitation = invitationManager.getByInvitationHash(userInfo.getHash());
        if (invitation == null || invitation.isOlderThan(invitationLengthInDays)) {
            throw new ValidationException(msgUtil.getMessage("user.invitation.expired",
                    invitationLengthInDays + "",
                    invitationLengthInDays == 1 ? "" : "s"));
        }

        return invitationManager.createUserFromInvitation(invitation, userInfo.getUser());
    }

    private Set<String> validateCreateUserFromInvitationRequest(CreateUserFromInvitationRequest request) {
        Set<String> validationErrors = new HashSet<String>();

        if (request.getUser().getFullName().length() > msgUtil.getMessageAsInteger("maxLength.fullName")) {
            validationErrors.add(msgUtil.getMessage("user.fullName.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.fullName")));
        }
        if (!StringUtils.isEmpty(request.getUser().getFriendlyName())
                && request.getUser().getFriendlyName().length() > msgUtil.getMessageAsInteger("maxLength.friendlyName")) {
            validationErrors.add(msgUtil.getMessage("user.friendlyName.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.friendlyName")));
        }
        if (!StringUtils.isEmpty(request.getUser().getTitle())
                && request.getUser().getTitle().length() > msgUtil.getMessageAsInteger("maxLength.title")) {
            validationErrors.add(msgUtil.getMessage("user.title.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.title")));
        }
        if (request.getUser().getEmail().length() > msgUtil.getMessageAsInteger("maxLength.email")) {
            validationErrors.add(msgUtil.getMessage("user.email.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.email")));
        }
        if (!StringUtils.isEmpty(request.getUser().getPhoneNumber())
                && request.getUser().getPhoneNumber().length() > msgUtil.getMessageAsInteger("maxLength.phoneNumber")) {
            validationErrors.add(msgUtil.getMessage("user.phoneNumber.maxlength",
                    msgUtil.getMessageAsInteger("maxLength.phoneNumber")));
        }
        return validationErrors;
    }

    @Operation(summary = "Confirm that a user's email address is valid.",
            description = "When a new user accepts their invitation to the CHPL they have to provide "
                    + "an email address. They then receive an email prompting them to confirm "
                    + "that this email address is valid. Confirming the email address must be done "
                    + "via this request before the user will be allowed to log in with "
                    + "the credentials they selected. " + "The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize 3) /confirm ",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)
            })
    @RequestMapping(value = "/confirm", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public User confirmUser(@RequestBody String hash) throws InvalidArgumentsException, UserRetrievalException,
    EntityRetrievalException, MessagingException, JsonProcessingException, EntityCreationException,
    MultipleUserAccountsException {
        UserInvitation invitation = invitationManager.getByConfirmationHash(hash);

        if (invitation == null || invitation.isOlderThan(confirmationLengthInDays)) {
            throw new InvalidArgumentsException(msgUtil.getMessage("user.confirmation.expired",
                    confirmationLengthInDays + "",
                    confirmationLengthInDays == 1 ? "" : "s"));
        }
        UserDTO createdUser = invitationManager.confirmAccountEmail(invitation);
        return new User(createdUser);
    }

    @Operation(summary = "Update an existing user account with new permissions.",
            description = "Gives the user permission on the object in the invitation (usually an additional ACB or ATL)."
                    + "The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize 3) /confirm.  Security Restrictions: ROLE_ADMIN "
                    + "or ROLE_ONC.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{userId}/authorize", method = RequestMethod.POST,
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public String authorizeUser(@RequestBody AuthorizeCredentials credentials)
            throws InvalidArgumentsException, JWTCreationException, UserRetrievalException,
            EntityRetrievalException, MultipleUserAccountsException, ChplAccountEmailNotConfirmedException {

        if (StringUtils.isEmpty(credentials.getHash())) {
            throw new InvalidArgumentsException("User key is required.");
        }

        JWTAuthenticatedUser loggedInUser = (JWTAuthenticatedUser) AuthUtil.getCurrentUser();
        if (loggedInUser == null
                && (StringUtils.isEmpty(credentials.getUserName()) || StringUtils.isEmpty(credentials.getPassword()))) {
            throw new InvalidArgumentsException(
                    "Username and Password are required since no user is currently logged in.");
        }

        UserInvitation invitation = invitationManager.getByInvitationHash(credentials.getHash());
        if (invitation == null || invitation.isOlderThan(authorizationLengthInDays)) {
            throw new InvalidArgumentsException(msgUtil.getMessage("user.invitation.expired",
                    authorizationLengthInDays + "",
                    authorizationLengthInDays == 1 ? "" : "s"));
        }

        // Log the user in, if they are not logged in
        if (Objects.isNull(loggedInUser)) {
            String jwt = authenticationManager.authenticate(credentials);
            if (jwt == null) {
                throw new ChplAccountStatusException(msgUtil.getMessage("auth.loginNotAllowed"));
            }
            UserDTO user = userManager.getByNameOrEmail(credentials.getUserName());
            Authentication invitedUserAuthenticator = AuthUtil.getInvitedUserAuthenticator(user.getId());
            SecurityContextHolder.getContext().setAuthentication(invitedUserAuthenticator);
            loggedInUser = (JWTAuthenticatedUser) AuthUtil.getCurrentUser();
        }

        UserDTO userToUpdate = userManager.getById(loggedInUser.getId());
        if (loggedInUser.getImpersonatingUser() != null) {
            userToUpdate = loggedInUser.getImpersonatingUser();
        }
        invitationManager.updateUserFromInvitation(new UserInvitationDTO(userToUpdate, invitation));
        UserDTO updatedUser = userManager.getById(userToUpdate.getId());
        return "{\"token\": \"" + authenticationManager.getJWT(updatedUser) + "\"}";
    }

    @Operation(summary = "Invite a user to the CHPL.",
            description = "This request creates an invitation that is sent to the email address provided. "
                    + "The recipient of this invitation can then choose to create a new account "
                    + "or add the permissions contained within the invitation to an existing account "
                    + "if they have one. Said another way, an invitation can be used to create or "
                    + "modify CHPL user accounts." + "The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize 3) /confirm. "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC can invite users to any organization.  "
                    + "ROLE_ACB and ROLE_ATL can add users to their own organization.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/invite", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public UserInvitation inviteUser(@RequestBody UserInvitation invitation)
            throws InvalidArgumentsException, UserCreationException, UserRetrievalException,
            UserPermissionRetrievalException, AddressException, EmailNotSentException {
        UserInvitation createdInvitiation = null;
        if (invitation.getRole().equals(Authority.ROLE_ADMIN)) {
            createdInvitiation = invitationManager.inviteAdmin(invitation.getEmailAddress());
        } else if (invitation.getRole().equals(Authority.ROLE_ONC)) {
            createdInvitiation = invitationManager.inviteOnc(invitation.getEmailAddress());
        } else if (invitation.getRole().equals(Authority.ROLE_ONC_STAFF)) {
            createdInvitiation = invitationManager.inviteOncStaff(invitation.getEmailAddress());
        } else if (invitation.getRole().equals(Authority.ROLE_CMS_STAFF)) {
            createdInvitiation = invitationManager.inviteCms(invitation.getEmailAddress());
        } else if (invitation.getRole().equals(Authority.ROLE_ACB)
                && invitation.getPermissionObjectId() != null) {
            createdInvitiation = invitationManager.inviteWithAcbAccess(invitation.getEmailAddress(),
                    invitation.getPermissionObjectId());
        } else if (invitation.getRole().equals(Authority.ROLE_ATL)
                && invitation.getPermissionObjectId() != null) {
            createdInvitiation = invitationManager.inviteWithAtlAccess(invitation.getEmailAddress(),
                    invitation.getPermissionObjectId());
        } else if (invitation.getRole().equals(Authority.ROLE_DEVELOPER)
                && invitation.getPermissionObjectId() != null) {
            createdInvitiation = invitationManager.inviteWithDeveloperAccess(invitation.getEmailAddress(),
                    invitation.getPermissionObjectId());
        }
        return createdInvitiation;
    }

    @Operation(summary = "Modify user information.", description = "",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = "application/json; charset=utf-8")
    public User updateUserDetails(@RequestBody User userInfo)
            throws UserRetrievalException, UserPermissionRetrievalException, JsonProcessingException,
            EntityCreationException, EntityRetrievalException, ValidationException, UserAccountExistsException,
            MultipleUserAccountsException {

        if (userInfo.getUserId() <= 0) {
            throw new UserRetrievalException("Cannot update user with ID less than 0");
        }

        UserDTO updated = userManager.update(userInfo);
        return new User(updated);
    }

    @Operation(summary = "Delete a user.",
            description = "Deletes a user account and all associated authorities on ACBs and ATLs. "
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public DeletedUser deleteUser(@PathVariable("userId") Long userId)
            throws UserRetrievalException, UserManagementException, UserPermissionRetrievalException,
            JsonProcessingException, EntityCreationException, EntityRetrievalException {

        if (userId <= 0) {
            throw new UserRetrievalException("Cannot delete user with ID less than 0");
        }
        UserDTO toDelete = userManager.getById(userId);

        if (toDelete == null) {
            throw new UserRetrievalException("Could not find user with id " + userId);
        }
        userManager.delete(toDelete);
        return new DeletedUser(true);
    }

    @Operation(summary = "View users of the system.",
            description = "Security Restrictions: ROLE_ADMIN and ROLE_ONC can see all users.  ROLE_ACB, ROLE_ATL, "
                    + "and ROLE_CMS_STAFF can see themselves.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    @PreAuthorize("isAuthenticated()")
    public @ResponseBody UsersResponse getUsers() {
        List<UserDTO> userList = userManager.getAll();
        List<User> users = new ArrayList<User>(userList.size());

        for (UserDTO userDto : userList) {
            User user = new User(userDto);
            users.add(user);
        }

        UsersResponse response = new UsersResponse();
        response.setUsers(users);
        return response;
    }

    @Deprecated
    @Operation(summary = "DEPRECATED. View a specific user's details.",
            description = "The logged in user must either be the user in the parameters, have ROLE_ADMIN, or "
                    + "have ROLE_ACB.",
            deprecated = true,
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/{userName}/details", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody User getUserByUsername(@PathVariable("userName") String userName)
            throws UserRetrievalException, MultipleUserAccountsException {
        UserDTO user = userManager.getByNameOrEmail(userName);
        if (user != null && user.getId() != null) {
            return userManager.getUserInfo(user.getId());
        }
        throw new UsernameNotFoundException("The user " + userName + " was not found.");
    }

    @Operation(summary = "View a specific user's details.",
            description = "The logged in user must either be the user in the parameters, have ROLE_ADMIN, or "
                    + "have ROLE_ACB.",
            security = {
                    @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER)
            })
    @RequestMapping(value = "/beta/{id}/details", method = RequestMethod.GET,
            produces = "application/json; charset=utf-8")
    public @ResponseBody User getUser(@PathVariable("id") Long id)
            throws UserRetrievalException {

        return userManager.getUserInfo(id);
    }

    private class DeletedUser {
        @Getter
        private Boolean deletedUser;

        DeletedUser(Boolean deletedUser) {
            this.deletedUser = deletedUser;
        }
    }
}
