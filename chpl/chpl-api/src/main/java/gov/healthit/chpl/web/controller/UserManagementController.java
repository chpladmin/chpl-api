package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.commons.lang.NotImplementedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.auth.authentication.Authenticator;
import gov.healthit.chpl.auth.user.JWTAuthenticatedUser;
import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.AuthorizeCredentials;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.domain.auth.UsersResponse;
import gov.healthit.chpl.domain.complaint.Complaint;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.auth.InvitationDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserInvitationDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.JWTCreationException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserManagementException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.InvitationManager;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.web.controller.results.ComplaintResults;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "users")
@RestController
@RequestMapping("/users")
public class UserManagementController {

    @Autowired
    private UserManager userManager;

    @Autowired
    private InvitationManager invitationManager;

    @Autowired
    private Authenticator authenticator;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private FF4j ff4j;

    @Autowired
    private Environment env;

    @Autowired
    private ErrorMessageUtil errorMessageUtil;

    @Autowired
    private UserPermissionsManager userPermissionsManager;

    @Autowired
    private ResourcePermissions resourcePermissions;

    private static final Logger LOGGER = LogManager.getLogger(UserManagementController.class);
    private static final long VALID_INVITATION_LENGTH = 3L * 24L * 60L * 60L * 1000L;
    private static final long VALID_CONFIRMATION_LENGTH = 30L * 24L * 60L * 60L * 1000L;

    @ApiOperation(value = "Create a new user account from an invitation.",
            notes = "An individual who has been invited to the CHPL has a special user key in their invitation email. "
                    + "That user key along with all the information needed to create a new user's account "
                    + "can be passed in here. The account is created but cannot be used until that user "
                    + "confirms that their email address is valid. The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize 3) /confirm ")
    @RequestMapping(value = "/create", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public @ResponseBody User createUser(@RequestBody final CreateUserFromInvitationRequest userInfo)
            throws ValidationException, EntityRetrievalException, InvalidArgumentsException, UserRetrievalException,
            UserCreationException, MessagingException, JsonProcessingException, EntityCreationException {

        if (userInfo.getUser() == null || userInfo.getUser().getSubjectName() == null) {
            throw new ValidationException(errorMessageUtil.getMessage("user.subjectName.required"));
        }

        Set<String> errors = validateCreateUserFromInvitationRequest(userInfo);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }

        InvitationDTO invitation = invitationManager.getByInvitationHash(userInfo.getHash());
        if (invitation == null || invitation.isOlderThan(VALID_INVITATION_LENGTH)) {
            throw new ValidationException(errorMessageUtil.getMessage("user.providerKey.invalid"));
        }

        UserDTO createdUser = invitationManager.createUserFromInvitation(invitation, userInfo.getUser());

        // get the invitation again to get the new hash
        invitation = invitationManager.getById(invitation.getId());

        // send email for user to confirm email address
        String htmlMessage = "<p>Thank you for setting up your administrator account on ONC's CHPL. "
                + "Please click the link below to activate your account: <br/>" + env.getProperty("chplUrlBegin")
                + "/#/registration/confirm-user/" + invitation.getConfirmToken() + "</p>"
                + "<p>If you have any issues completing the registration, "
                + "please contact the ONC CHPL Team at <a href=\"mailto:onc_chpl@hhs.gov\">onc_chpl@hhs.gov</a>.</p>"
                + "<p>The CHPL Team</p>";

        String[] toEmails = {
                createdUser.getEmail()
        };
        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(new ArrayList<String>(Arrays.asList(toEmails)))
        .subject("Confirm CHPL Administrator Account")
        .htmlMessage(htmlMessage)
        .sendEmail();

        String activityDescription = "User " + createdUser.getSubjectName() + " was created.";
        activityManager.addActivity(ActivityConcept.USER, createdUser.getId(), activityDescription,
                null, createdUser, createdUser.getId());

        User result = new User(createdUser);
        result.setHash(invitation.getConfirmToken());
        return result;
    }

    private Set<String> validateCreateUserFromInvitationRequest(final CreateUserFromInvitationRequest request) {
        Set<String> validationErrors = new HashSet<String>();

        if (request.getUser().getSubjectName().length() > errorMessageUtil.getMessageAsInteger("maxLength.subjectName")) {
            validationErrors.add(errorMessageUtil.getMessage("user.subjectName.maxlength",
                    errorMessageUtil.getMessageAsInteger("maxLength.subjectName")));
        }
        if (request.getUser().getFullName().length() > errorMessageUtil.getMessageAsInteger("maxLength.fullName")) {
            validationErrors.add(errorMessageUtil.getMessage("user.fullName.maxlength",
                    errorMessageUtil.getMessageAsInteger("maxLength.fullName")));
        }
        if (!StringUtils.isEmpty(request.getUser().getFriendlyName())
                && request.getUser().getFriendlyName().length() > errorMessageUtil.getMessageAsInteger("maxLength.friendlyName")) {
            validationErrors.add(errorMessageUtil.getMessage("user.friendlyName.maxlength",
                    errorMessageUtil.getMessageAsInteger("maxLength.friendlyName")));
        }
        if (!StringUtils.isEmpty(request.getUser().getTitle())
                && request.getUser().getTitle().length() > errorMessageUtil.getMessageAsInteger("maxLength.title")) {
            validationErrors.add(errorMessageUtil.getMessage("user.title.maxlength",
                    errorMessageUtil.getMessageAsInteger("maxLength.title")));
        }
        if (request.getUser().getEmail().length() > errorMessageUtil.getMessageAsInteger("maxLength.email")) {
            validationErrors.add(errorMessageUtil.getMessage("user.email.maxlength",
                    errorMessageUtil.getMessageAsInteger("maxLength.email")));
        }
        if (request.getUser().getPhoneNumber().length() > errorMessageUtil.getMessageAsInteger("maxLength.phoneNumber")) {
            validationErrors.add(errorMessageUtil.getMessage("user.phoneNumber.maxlength",
                    errorMessageUtil.getMessageAsInteger("maxLength.phoneNumber")));
        }
        return validationErrors;
    }

    @ApiOperation(value = "Confirm that a user's email address is valid.",
            notes = "When a new user accepts their invitation to the CHPL they have to provide "
                    + "an email address. They then receive an email prompting them to confirm "
                    + "that this email address is valid. Confirming the email address must be done "
                    + "via this request before the user will be allowed to log in with "
                    + "the credentials they selected. " + "The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize 3) /confirm ")
    @RequestMapping(value = "/confirm", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public User confirmUser(@RequestBody final String hash) throws InvalidArgumentsException, UserRetrievalException,
    EntityRetrievalException, MessagingException, JsonProcessingException, EntityCreationException {
        InvitationDTO invitation = invitationManager.getByConfirmationHash(hash);

        if (invitation == null || invitation.isOlderThan(VALID_INVITATION_LENGTH)) {
            throw new InvalidArgumentsException(
                    "Provided user key is not valid in the database. The user key is valid for up to 3 days from when "
                            + "it is assigned.");
        }
        UserDTO createdUser = invitationManager.confirmAccountEmail(invitation);
        return new User(createdUser);
    }

    @ApiOperation(value = "Update an existing user account with new permissions.",
            notes = "Gives the user permission on the object in the invitation (usually an additional ACB or ATL)."
                    + "The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize 3) /confirm.  Security Restrictions: ROLE_ADMIN "
                    + "or ROLE_ONC.")
    @RequestMapping(value = "/{userId}/authorize", method = RequestMethod.POST,
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public String authorizeUser(@RequestBody final AuthorizeCredentials credentials)
            throws InvalidArgumentsException, JWTCreationException, UserRetrievalException, EntityRetrievalException {

        if (StringUtils.isEmpty(credentials.getHash())) {
            throw new InvalidArgumentsException("User key is required.");
        }

        JWTAuthenticatedUser loggedInUser = (JWTAuthenticatedUser) AuthUtil.getCurrentUser();
        if (loggedInUser == null
                && (StringUtils.isEmpty(credentials.getUserName()) || StringUtils.isEmpty(credentials.getPassword()))) {
            throw new InvalidArgumentsException(
                    "Username and Password are required since no user is currently logged in.");
        }

        InvitationDTO invitation = invitationManager.getByInvitationHash(credentials.getHash());
        if (invitation == null || invitation.isOlderThan(VALID_CONFIRMATION_LENGTH)) {
            throw new InvalidArgumentsException(
                    "Provided user key is not valid in the database. The user key is valid for up to 3 days from when "
                            + "it is assigned.");
        }

        String jwtToken = null;
        if (loggedInUser == null) {
            //there's no logged in user - they are logging in at the same time
            //they are accepting the invitation for additional access
            UserDTO userToUpdate = authenticator.getUser(credentials);
            if (userToUpdate == null) {
                throw new UserRetrievalException(
                        "The user " + credentials.getUserName() + " could not be authenticated.");
            }
            //invitation manager method has security on it
            //so it cannot be called without some user in the security context;
            //in this case the user is logging in at the same time as accepting the invitation
            //so there will not be any user in the security context until the call completes
            //put in this one so that the permissions security can work
            Authentication invitedUserAuthenticator =
                    AuthUtil.getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
            SecurityContextHolder.getContext().setAuthentication(invitedUserAuthenticator);
            invitationManager.updateUserFromInvitation(new UserInvitationDTO(userToUpdate, invitation));
            SecurityContextHolder.getContext().setAuthentication(null);
            jwtToken = authenticator.getJWT(credentials);
        } else {
            // add authorization to the currently logged in user
            UserDTO userToUpdate = userManager.getById(loggedInUser.getId());
            if (loggedInUser.getImpersonatingUser() != null) {
                userToUpdate = loggedInUser.getImpersonatingUser();
            }
            invitationManager.updateUserFromInvitation(new UserInvitationDTO(userToUpdate, invitation));
            UserDTO updatedUser = userManager.getById(userToUpdate.getId());
            jwtToken = authenticator.getJWT(updatedUser);
        }

        String jwtJSON = "{\"token\": \"" + jwtToken + "\"}";
        return jwtJSON;
    }

    @ApiOperation(value = "Invite a user to the CHPL.",
            notes = "This request creates an invitation that is sent to the email address provided. "
                    + "The recipient of this invitation can then choose to create a new account "
                    + "or add the permissions contained within the invitation to an existing account "
                    + "if they have one. Said another way, an invitation can be used to create or "
                    + "modify CHPL user accounts." + "The correct order to call invitation requests is "
                    + "the following: 1) /invite 2) /create or /authorize 3) /confirm. "
                    + "Security Restrictions: ROLE_ADMIN and ROLE_ONC can invite users to any organization.  "
                    + "ROLE_ACB and ROLE_ATL can add users to their own organization.")
    @RequestMapping(value = "/invite", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public UserInvitation inviteUser(@RequestBody final UserInvitation invitation)
            throws InvalidArgumentsException, UserCreationException, UserRetrievalException,
            UserPermissionRetrievalException, AddressException, MessagingException {

        if (!ff4j.check(FeatureList.ROLE_DEVELOPER) && invitation.getRole().equals(Authority.ROLE_DEVELOPER)) {
            throw new NotImplementedException();
        }

        InvitationDTO createdInvite = null;
        if (invitation.getRole().equals(Authority.ROLE_ADMIN)) {
            createdInvite = invitationManager.inviteAdmin(invitation.getEmailAddress());
        } else if (invitation.getRole().equals(Authority.ROLE_ONC)) {
            createdInvite = invitationManager.inviteOnc(invitation.getEmailAddress());
        } else if (invitation.getRole().equals(Authority.ROLE_CMS_STAFF)) {
            createdInvite = invitationManager.inviteCms(invitation.getEmailAddress());
        } else if (invitation.getRole().equals(Authority.ROLE_ACB)
                    && invitation.getPermissionObjectId() != null) {
            createdInvite = invitationManager.inviteWithAcbAccess(invitation.getEmailAddress(),
                    invitation.getPermissionObjectId());
        } else if (invitation.getRole().equals(Authority.ROLE_ATL)
                    && invitation.getPermissionObjectId() != null) {
                createdInvite = invitationManager.inviteWithAtlAccess(invitation.getEmailAddress(),
                        invitation.getPermissionObjectId());
        } else if (invitation.getRole().equals(Authority.ROLE_DEVELOPER)
                && invitation.getPermissionObjectId() != null) {
            createdInvite = invitationManager.inviteWithDeveloperAccess(invitation.getEmailAddress(),
                    invitation.getPermissionObjectId());
    }

        // send email
        String htmlMessage = "<p>Hi,</p>" + "<p>You have been granted a new role on ONC's CHPL "
                + "which will allow you to manage certified product listings on the CHPL. "
                + "Please click the link below to create or update your account: <br/>"
                + env.getProperty("chplUrlBegin") + "/#/registration/create-user/" + createdInvite.getInviteToken()
                + "</p>"
                + "<p>If you have any issues completing the registration, "
                + "please contact the ONC CHPL Team at <a href=\"mailto:onc_chpl@hhs.gov\">onc_chpl@hhs.gov</a>.</p>"
                + "<p>Take care,<br/> " + "The CHPL Team</p>";

        String[] toEmails = {
                createdInvite.getEmail()
        };

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(new ArrayList<String>(Arrays.asList(toEmails)))
        .subject("CHPL Administrator Invitation")
        .htmlMessage(htmlMessage)
        .sendEmail();

        UserInvitation result = new UserInvitation(createdInvite);
        return result;
    }

    @ApiOperation(value = "Modify user information.", notes = "")
    @RequestMapping(value = "/{userId}", method = RequestMethod.PUT, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public User updateUserDetails(@RequestBody final User userInfo)
            throws UserRetrievalException, UserPermissionRetrievalException, JsonProcessingException,
            EntityCreationException, EntityRetrievalException {

        if (userInfo.getUserId() <= 0) {
            throw new UserRetrievalException("Cannot update user with ID less than 0");
        }

        UserDTO before = userManager.getById(userInfo.getUserId());
        UserDTO toUpdate = new UserDTO();
        toUpdate.setId(before.getId());
        toUpdate.setPasswordResetRequired(userInfo.getPasswordResetRequired());
        toUpdate.setAccountEnabled(userInfo.getAccountEnabled());
        toUpdate.setAccountExpired(before.isAccountExpired());
        toUpdate.setAccountLocked(userInfo.getAccountLocked());
        toUpdate.setCredentialsExpired(userInfo.getCredentialsExpired());
        toUpdate.setEmail(userInfo.getEmail());
        toUpdate.setFailedLoginCount(before.getFailedLoginCount());
        toUpdate.setFriendlyName(userInfo.getFriendlyName());
        toUpdate.setFullName(userInfo.getFullName());
        toUpdate.setPasswordResetRequired(userInfo.getPasswordResetRequired());
        toUpdate.setPermission(before.getPermission());
        toUpdate.setPhoneNumber(userInfo.getPhoneNumber());
        toUpdate.setSignatureDate(before.getSignatureDate());
        toUpdate.setSubjectName(before.getSubjectName());
        toUpdate.setTitle(userInfo.getTitle());
        UserDTO updated = userManager.update(toUpdate);

        String activityDescription = "User " + userInfo.getSubjectName() + " was updated.";
        activityManager.addActivity(ActivityConcept.USER, before.getId(), activityDescription, before,
                updated);

        return new User(updated);
    }

    @ApiOperation(value = "Delete a user.",
            notes = "Deletes a user account and all associated authorities on ACBs and ATLs. "
                    + "Security Restrictions: ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public String deleteUser(@PathVariable("userId") final Long userId)
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
        //db soft delete trigger takes care of deleting things associated with this user.

        String activityDescription = "User " + toDelete.getSubjectName() + " was deleted.";
        activityManager.addActivity(ActivityConcept.USER, toDelete.getId(), activityDescription,
                toDelete, null);

        return "{\"deletedUser\" : true}";
    }

    @ApiOperation(value = "View users of the system.",
            notes = "Security Restrictions: ROLE_ADMIN and ROLE_ONC can see all users.  ROLE_ACB, ROLE_ATL, "
                    + "and ROLE_CMS_STAFF can see their self.")
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

    @ApiOperation(value = "View a specific user's details.",
            notes = "The logged in user must either be the user in the parameters, have ROLE_ADMIN, or "
                    + "have ROLE_ACB.")
    @RequestMapping(value = "/{userName}/details", method = RequestMethod.GET,
    produces = "application/json; charset=utf-8")
    public @ResponseBody User getUser(@PathVariable("userName") final String userName)
            throws UserRetrievalException {

        return userManager.getUserInfo(userName);
    }
}
