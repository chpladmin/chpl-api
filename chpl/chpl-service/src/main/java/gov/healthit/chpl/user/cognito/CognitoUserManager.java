package gov.healthit.chpl.user.cognito;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CreateUserFromInvitationRequest;
import gov.healthit.chpl.domain.Organization;
import gov.healthit.chpl.domain.auth.CognitoEnvironments;
import gov.healthit.chpl.domain.auth.CognitoGroups;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.ActivityException;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.user.cognito.invitation.CognitoInvitationManager;
import gov.healthit.chpl.user.cognito.invitation.CognitoUserInvitation;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class CognitoUserManager {
    private static final String NON_PROD_ENVIRONMENT = "non-production";

    private CognitoUserCreationValidator userCreationValidator;
    private CognitoUpdateUserValidator userUpdateValidator;
    private CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer;
    private CognitoApiWrapper cognitoApiWrapper;
    private CognitoInvitationManager cognitoInvitationManager;
    private String groupNameForEnvironment;
    private Long invitationLengthDays;
    private ErrorMessageUtil errorMessageUtil;
    private ActivityManager activityManager;
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private boolean isProdEnvironment = true;

    @Autowired
    public CognitoUserManager(CognitoUserCreationValidator userCreationValidator,
            CognitoConfirmEmailEmailer cognitoConfirmEmailEmailer,
            CognitoUpdateUserValidator userUpdateValidator,
            CognitoApiWrapper cognitoApiWrapper,
            CognitoInvitationManager cognitoInvitationManager,
            ActivityManager activityManager,
            ResourcePermissionsFactory resourcePermissionsFactory,
            ErrorMessageUtil errorMessageUtil,
            @Value("${cognito.environment.groupName}") String groupNameForEnvironment,
            @Value("${invitationLengthInDays}") Long invitationLengthDays,
            @Value("${server.environment}") String serverEnvironment) {

        this.userCreationValidator = userCreationValidator;
        this.userUpdateValidator = userUpdateValidator;
        this.cognitoConfirmEmailEmailer = cognitoConfirmEmailEmailer;
        this.cognitoApiWrapper = cognitoApiWrapper;
        this.cognitoInvitationManager = cognitoInvitationManager;
        this.errorMessageUtil = errorMessageUtil;
        this.groupNameForEnvironment = groupNameForEnvironment;
        this.invitationLengthDays = invitationLengthDays;
        this.activityManager = activityManager;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.groupNameForEnvironment = groupNameForEnvironment;
        if (StringUtils.equals(serverEnvironment, NON_PROD_ENVIRONMENT)) {
            isProdEnvironment = false;
        }
    }

    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME)")
    @PostAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_BY_USER_NAME, returnObject)")
    public User getUserInfo(UUID cognitoId) throws UserRetrievalException {
        return cognitoApiWrapper.getUserInfo(cognitoId);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).UPDATE_COGNITO, #user)")
    public User updateUser(User user) throws ValidationException, UserRetrievalException, ActivityException {
        Set<String> errors = userUpdateValidator.validate(user);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }

        User originalUser = cognitoApiWrapper.getUserNoCache(user.getCognitoId());
        cognitoApiWrapper.updateUser(user);

        //check for organizations to remove (organizations are never added here, only removed)
        List<Long> removedOrganizationIds = getRemovedOrganizationIds(originalUser, user);
        if (!CollectionUtils.isEmpty(removedOrganizationIds)) {
            cognitoApiWrapper.removeOrgsFromUser(originalUser, removedOrganizationIds);
        }

        if (userShouldBeDisabled(originalUser, user)) {
            cognitoApiWrapper.disableUser(user);
        } else if (userShouldBeEnabled(originalUser, user)) {
            cognitoApiWrapper.enableUser(user);
        }

        User updatedUser = cognitoApiWrapper.getUserNoCache(user.getCognitoId());
        activityManager.addUserActivity(updatedUser.getCognitoId(),
                String.format("User %s was updated", updatedUser.getEmail()),
                originalUser, updatedUser);
        return updatedUser;
    }

    private List<Long> getRemovedOrganizationIds(User originalUser, User updatedUser) {
        List<Organization> removedOrgs = subtractLists(originalUser.getOrganizations(), updatedUser.getOrganizations());
        if (!CollectionUtils.isEmpty(removedOrgs)) {
            return removedOrgs.stream()
                    .map(org -> org.getId())
                    .collect(Collectors.toList());
        }
        return null;
    }

    private List<Organization> subtractLists(List<Organization> listA, List<Organization> listB) {
        Predicate<Organization> notInListB = orgFromA -> !listB.stream()
                .anyMatch(orgFromB -> orgFromA.getId().equals(orgFromB.getId()));

        return listA.stream()
                .filter(notInListB)
                .collect(Collectors.toList());
    }

    private boolean userShouldBeDisabled(User originalUser, User updatedUser) {
        //If there are no organizations remaining for this user and the user is an acb or developer
        //then they should be disabled.
        //They should also be disabled if the update to the specifically went from enabled to disabled.
        if (originalUser.getAccountEnabled() && !updatedUser.getAccountEnabled()) {
            return true;
        } else if (!CollectionUtils.isEmpty(resourcePermissionsFactory.get().getAllAcbsForUser(originalUser))
                && CollectionUtils.isEmpty(resourcePermissionsFactory.get().getAllAcbsForUser(updatedUser))) {
            return true;
        } else if (!CollectionUtils.isEmpty(resourcePermissionsFactory.get().getAllDevelopersForUser(originalUser))
                && CollectionUtils.isEmpty(resourcePermissionsFactory.get().getAllDevelopersForUser(updatedUser))) {
            return true;
        }
        return false;
    }

    private boolean userShouldBeEnabled(User originalUser, User updatedUser) {
        return !originalUser.getAccountEnabled() && updatedUser.getAccountEnabled();
    }

    @Transactional
    public UUID createUser(CreateUserFromInvitationRequest userInfo)
            throws ValidationException, UserCreationException, UserRetrievalException, ActivityException, EmailNotSentException {

        Set<String> errors = userCreationValidator.validate(userInfo);
        if (errors.size() > 0) {
            throw new ValidationException(errors, null);
        }

        // Need to be able to rollback this whole thing if there is an error...
        CognitoCredentials credentials = null;
        try {
            CognitoUserInvitation invitation = cognitoInvitationManager.getByToken(UUID.fromString(userInfo.getHash()));

            //if the user exists for this environment and is disabled, we will re-enable them
            //otherwise we will create a brand new user
            User existingUser = null;
            try {
                existingUser = cognitoApiWrapper.getUserInfo(userInfo.getUser().getEmail());
            } catch (Exception ex) {
                LOGGER.warn("Unable to look up user with email address " + userInfo.getUser().getEmail());
            }

            if (existingUser != null && BooleanUtils.isFalse(existingUser.getAccountEnabled())) {
                credentials = reenableUser(userInfo, invitation, existingUser);
            } else if (existingUser == null) {
                credentials = createNewUser(userInfo, invitation);
            } else {
                LOGGER.warn("The user with email address " + userInfo.getUser().getEmail() + " already exists and is enabled. "
                        + "A new account cannot be created.");
            }

            if (credentials != null) {
                cognitoConfirmEmailEmailer.sendConfirmationEmail(credentials);
            }
        } catch (Exception e) {
            //Invitation deletion should roll back due to @Transactional
            if (credentials != null) {
                cognitoApiWrapper.deleteUser(credentials.getCognitoId());
            }
            throw e;
        }
        return credentials == null ? null : credentials.getCognitoId();
    }

    private CognitoCredentials reenableUser(CreateUserFromInvitationRequest userInfo, CognitoUserInvitation invitation,
            User existingUser) throws UserRetrievalException, UserCreationException, EmailNotSentException, ActivityException {
        LOGGER.info("Re-enabling user " + existingUser.getEmail() + " from invitation " + userInfo.getHash());

        existingUser.setFullName(userInfo.getUser().getFullName());
        CognitoCredentials credentials = cognitoApiWrapper.reenableUser(existingUser);
        if (invitation.getOrganizationId() != null) {
            cognitoApiWrapper.addOrgToUser(existingUser, invitation.getOrganizationId());
        }

        cognitoApiWrapper.updateUser(existingUser);
        cognitoInvitationManager.deleteInvitation(invitation);

        User reenabledUser = cognitoApiWrapper.getUserNoCache(existingUser.getCognitoId());
        activityManager.addUserActivity(reenabledUser.getCognitoId(),
                String.format("User %s was re-enabled", reenabledUser.getEmail()),
                existingUser, reenabledUser);

        return credentials;
    }

    private CognitoCredentials createNewUser(CreateUserFromInvitationRequest userInfo, CognitoUserInvitation invitation)
            throws UserRetrievalException, UserCreationException, EmailNotSentException, ActivityException {
        LOGGER.info("Creating new user " + userInfo.getUser().getEmail() + " from invitation " + userInfo.getHash());
        if (invitation.getOrganizationId() != null) {
            userInfo.getUser().setOrganizationId(invitation.getOrganizationId());
        }
        CognitoCredentials credentials = cognitoApiWrapper.createUser(userInfo.getUser());
        cognitoApiWrapper.addUserToGroup(userInfo.getUser().getEmail(), invitation.getGroupName());
        if (isProdEnvironment) {
            addUserToAppropriateEnvironments(userInfo.getUser().getEmail(), invitation.getGroupName());
        } else {
            cognitoApiWrapper.addUserToGroup(userInfo.getUser().getEmail(), groupNameForEnvironment);
        }
        cognitoInvitationManager.deleteInvitation(invitation);

        User createdUser = cognitoApiWrapper.getUserNoCache(credentials.getCognitoId());
        activityManager.addUserActivity(createdUser.getCognitoId(),
                String.format("User %s was created", createdUser.getEmail()),
                null, createdUser);

        return credentials;
    }

    @Transactional
    @PostFilter("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).GET_ALL, filterObject)")
    public List<User> getAll() {
        return cognitoApiWrapper.getAllUsers();
    }

    private void addUserToAppropriateEnvironments(String userEmail, String userRole) {
        switch (userRole) {
            case CognitoGroups.CHPL_ADMIN:
            case CognitoGroups.CHPL_ONC:
                cognitoApiWrapper.addUserToGroup(userEmail, CognitoEnvironments.DEV.getName());
                cognitoApiWrapper.addUserToGroup(userEmail, CognitoEnvironments.QA.getName());
                cognitoApiWrapper.addUserToGroup(userEmail, CognitoEnvironments.STG.getName());
                cognitoApiWrapper.addUserToGroup(userEmail, CognitoEnvironments.PROD.getName());
                break;
            case CognitoGroups.CHPL_ACB:
                cognitoApiWrapper.addUserToGroup(userEmail, CognitoEnvironments.STG.getName());
                cognitoApiWrapper.addUserToGroup(userEmail, CognitoEnvironments.PROD.getName());
                break;
            case CognitoGroups.CHPL_DEVELOPER:
            case CognitoGroups.CHPL_CMS_STAFF:
                cognitoApiWrapper.addUserToGroup(userEmail, CognitoEnvironments.PROD.getName());
                break;
            default:
                LOGGER.error("User role '" + userRole + "' is not recognized. The user '" + userEmail + "' will not have access to any environments.");
                break;
        }
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).SECURED_USER, "
            + "T(gov.healthit.chpl.permissions.domains.SecuredUserDomainPermissions).ADD_ORG_TO_USER)")
    public User addOrganizationToUser(UUID invitationToken, String accessToken) throws InvalidArgumentsException, UserRetrievalException, ActivityException {

        User originalUser = cognitoApiWrapper.getUserInfo(AuthUtil.getCurrentUser().getCognitoId());
        CognitoUserInvitation invitation = cognitoInvitationManager.getByToken(invitationToken);
        if (invitation == null || invitation.isOlderThan(invitationLengthDays)) {
            throw new InvalidArgumentsException(errorMessageUtil.getMessage("user.invitation.invalid",
                    invitationLengthDays + "",
                    invitationLengthDays == 1 ? "" : "s"));
        }

        cognitoApiWrapper.addOrgToUser(originalUser, invitation.getOrganizationId());
        cognitoInvitationManager.deleteInvitation(invitation);

        User updatedUser = cognitoApiWrapper.getUserNoCache(originalUser.getCognitoId());
        activityManager.addUserActivity(updatedUser.getCognitoId(),
                String.format("User %s was updated", updatedUser.getEmail()),
                originalUser, updatedUser);
        return updatedUser;
    }
}
