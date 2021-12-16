package gov.healthit.chpl.manager;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.auth.InvitationDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.CreateUserRequest;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.domain.auth.UserInvitation;
import gov.healthit.chpl.domain.auth.UserPermission;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserInvitationDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.manager.impl.SecuredManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.service.InvitationEmailer;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class InvitationManager extends SecuredManager {
    private InvitationDAO invitationDao;
    private UserDAO userDao;
    private UserManager userManager;
    private UserPermissionsManager userPermissionsManager;
    private InvitationEmailer invitationEmailer;
    private ActivityManager activityManager;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;
    private List<UserPermission> userPermissions;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public InvitationManager(UserPermissionDAO userPermissionDao, InvitationDAO invitationDao,
            UserDAO userDao, UserManager userManager, UserPermissionsManager userPermissionsManager,
            InvitationEmailer invitationEmailer, ActivityManager activityManager,
            ResourcePermissions resourcePermissions, ErrorMessageUtil msgUtil) {
        this.invitationDao = invitationDao;
        this.userDao = userDao;
        this.userManager = userManager;
        this.userPermissionsManager = userPermissionsManager;
        this.invitationEmailer = invitationEmailer;
        this.activityManager = activityManager;
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
        this.userPermissions = userPermissionDao.findAll();
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ADMIN)")
    public UserInvitation inviteAdmin(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return inviteToAccount(Authority.ROLE_ADMIN, null, emailAddress);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ONC)")
    public UserInvitation inviteOnc(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return inviteToAccount(Authority.ROLE_ONC, null, emailAddress);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ONC_STAFF)")
    public UserInvitation inviteOncStaff(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return inviteToAccount(Authority.ROLE_ONC_STAFF, null, emailAddress);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_CMS)")
    public UserInvitation inviteCms(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return inviteToAccount(Authority.ROLE_CMS_STAFF, null, emailAddress);

    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ACB, #acbId)")
    public UserInvitation inviteWithAcbAccess(String emailAddress, Long acbId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return inviteToAccount(Authority.ROLE_ACB, acbId, emailAddress);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ATL, #atlId)")
    public UserInvitation inviteWithAtlAccess(String emailAddress, Long atlId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return inviteToAccount(Authority.ROLE_ATL, atlId, emailAddress);

    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_DEVELOPER, #developerId)")
    public UserInvitation inviteWithDeveloperAccess(String emailAddress, Long developerId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        return inviteToAccount(Authority.ROLE_DEVELOPER, developerId, emailAddress);
    }

    private UserInvitation inviteToAccount(String authority, Long permissionObjectId, String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        Optional<UserPermission> userPermission = this.userPermissions.stream().filter(perm -> perm.getAuthority().equals(authority)).findAny();
        if (!userPermission.isPresent()) {
            throw new UserPermissionRetrievalException("No User Permission was found for authority " + authority);
        }

        UserInvitation invitation = UserInvitation.builder()
                .emailAddress(emailAddress)
                .permissionObjectId(permissionObjectId)
                .hash(Util.md5(emailAddress + System.currentTimeMillis()))
                .permission(userPermission.get())
                .build();
        LOGGER.info("Creating invitation for " + emailAddress);
        Long createdInvitationId = invitationDao.create(invitation);
        LOGGER.info("Created invitation for " + emailAddress + " with ID " + createdInvitationId);
        UserInvitation createdInvitation = invitationDao.getById(createdInvitationId);
        LOGGER.info("Emailing user...");
        invitationEmailer.emailInvitedUser(createdInvitation);
        LOGGER.info("Emailed " + emailAddress);
        return createdInvitation;
    }

    @Transactional
    public UserInvitation getByInvitationHash(String hash) {
        return invitationDao.getByInvitationToken(hash);
    }

    @Transactional
    public UserInvitation getByConfirmationHash(String hash) {
        return invitationDao.getByConfirmationToken(hash);
    }

    @Transactional
    public UserInvitation getById(Long id) throws UserRetrievalException {
        return invitationDao.getById(id);
    }

    @Transactional
    public UserInvitation getByCreatedUserId(Long createdUserId) {
        return invitationDao.getByCreatedUserId(createdUserId);
    }

    @Transactional
    public User createUserFromInvitation(UserInvitation invitation, CreateUserRequest user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException,
            UserCreationException, MultipleUserAccountsException, EntityCreationException, EntityRetrievalException,
            JsonProcessingException {
        Authentication authenticator = AuthUtil.getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
        SecurityContextHolder.getContext().setAuthentication(authenticator);

        try {
            UserDTO existingUser = userManager.getByNameOrEmail(user.getEmail());
            if (existingUser != null) {
                throw new InvalidArgumentsException(msgUtil.getMessage("user.accountAlreadyExists", user.getEmail()));
            }
        } catch (UserRetrievalException urex) {
            //ignore
        } catch (MultipleUserAccountsException ex) {
            //hitting this block means there are multiple users registered with this account.
            //don't let them make a new one!
            throw new InvalidArgumentsException(msgUtil.getMessage("user.accountAlreadyExists", user.getEmail()));
        }

        UserDTO toCreate = constructUser(invitation, user);
        UserDTO newUser = userManager.create(toCreate, user.getPassword());
        try {
            handleInvitation(invitation, newUser);

            // update invitation entity to change the hashes
            invitation.setCreatedUserId(newUser.getId());
            invitation.setInvitationToken(null);
            Date now = new Date();
            invitation.setConfirmationToken(Util.md5(invitation.getEmailAddress() + now.getTime()));
            invitationDao.update(invitation);
            invitationEmailer.emailNewUser(newUser, invitation);

            User result = new User(newUser);
            result.setHash(invitation.getConfirmationToken());
            return result;
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }

    }

    @Transactional
    public UserDTO confirmAccountEmail(UserInvitation invitation) throws UserRetrievalException, MultipleUserAccountsException {
        Authentication authenticator = AuthUtil.getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
        SecurityContextHolder.getContext().setAuthentication(authenticator);

        try {
            UserDTO origUser = userDao.getById(invitation.getCreatedUserId());

            // set the user signature date
            UserDTO user = userDao.getById(invitation.getCreatedUserId());
            if (user == null) {
                throw new UserRetrievalException(
                        "Could not find user created from invitation. Looking for user with id "
                                + invitation.getCreatedUserId());
            }
            user.setSignatureDate(new Date());
            userDao.update(user);
            invitationDao.delete(invitation.getId());

            String activityDescription = "User " + user.getEmail() + " was confirmed.";
            try {
                activityManager.addActivity(ActivityConcept.USER, user.getId(), activityDescription, origUser, user,
                        user.getId());
            } catch (JsonProcessingException | EntityCreationException | EntityRetrievalException e) {
                LOGGER.error("Error creating user confirmation activity.  UserId: " + user.getId(), e);
            }

            return user;
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).UPDATE_FROM_INVITATION, #userInvitation)")
    public UserDTO updateUserFromInvitation(UserInvitationDTO userInvitation)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException {
        gov.healthit.chpl.auth.user.User loggedInUser = gov.healthit.chpl.util.AuthUtil.getCurrentUser();

        // have to give temporary permission to see all ACBs and ATLs
        // because the logged in user wouldn't already have permission on them
        Authentication authenticator = AuthUtil
                .getInvitedUserAuthenticator(userInvitation.getInvitation().getLastModifiedUserId());
        SecurityContextHolder.getContext().setAuthentication(authenticator);

        handleInvitation(userInvitation.getInvitation(), userInvitation.getUser());
        invitationDao.delete(userInvitation.getInvitation().getId());

        // put the permissions back how they were
        if (loggedInUser == null) {
            SecurityContextHolder.getContext().setAuthentication(null);
        } else {
            SecurityContextHolder.getContext().setAuthentication(loggedInUser);
        }

        return userInvitation.getUser();
    }

    /**
     * Adds the invited user (who has now created an account) to any ACBs, ATLs, or Developers
     * that the invitation specifies they should have access to.
     * Also could be an existing user getting ACBs, ATLs, or Developers added to their account.
     * The securitycontext must have a valid authentication specified when this is called
     */
    private void handleInvitation(UserInvitation invitation, UserDTO user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException {
        CertificationBodyDTO userAcb = null;
        TestingLabDTO userAtl = null;
        DeveloperDTO userDeveloper = null;

        if (!StringUtils.isEmpty(invitation.getRole()) && invitation.getRole().equals(Authority.ROLE_ACB)
                && invitation.getPermissionObjectId() != null) {
            userAcb = resourcePermissions.getAcbIfPermissionById(invitation.getPermissionObjectId());
            if (userAcb == null) {
                throw new InvalidArgumentsException("Could not find ACB with id " + invitation.getPermissionObjectId());
            }
        } else if (!StringUtils.isEmpty(invitation.getRole())
                && invitation.getRole().equals(Authority.ROLE_ATL)
                && invitation.getPermissionObjectId() != null) {
            userAtl = resourcePermissions.getAtlIfPermissionById(invitation.getPermissionObjectId());
            if (userAtl == null) {
                throw new InvalidArgumentsException(
                        "Could not find the testing lab with id " + invitation.getPermissionObjectId());
            }
        } else if (!StringUtils.isEmpty(invitation.getRole())
                && invitation.getRole().equals(Authority.ROLE_DEVELOPER)
                && invitation.getPermissionObjectId() != null) {
            userDeveloper = resourcePermissions.getDeveloperIfPermissionById(invitation.getPermissionObjectId());
            if (userDeveloper == null) {
                throw new InvalidArgumentsException(
                        "Could not find the developer with id " + invitation.getPermissionObjectId());
            }
        }

        // give them access to the invited acb, atl, or developer
        if (userAcb != null) {
            userPermissionsManager.addAcbPermission(userAcb, user.getId());
        } else if (userAtl != null) {
            userPermissionsManager.addAtlPermission(userAtl, user.getId());
        } else if (userDeveloper != null) {
            userPermissionsManager.addDeveloperPermission(userDeveloper, user.getId());
        }
    }

    private UserDTO constructUser(UserInvitation invitation, CreateUserRequest user) {
        UserDTO userDto = new UserDTO();
        userDto.setTitle(user.getTitle());
        userDto.setPermission(invitation.getPermission());
        userDto.setPhoneNumber(user.getPhoneNumber());
        userDto.setPasswordResetRequired(false);
        userDto.setFullName(user.getFullName());
        userDto.setFriendlyName(user.getFriendlyName());
        userDto.setFailedLoginCount(0);
        userDto.setEmail(user.getEmail());
        userDto.setCredentialsExpired(false);
        userDto.setAccountLocked(false);
        userDto.setAccountExpired(false);
        userDto.setAccountEnabled(true);
        return userDto;
    }
}
