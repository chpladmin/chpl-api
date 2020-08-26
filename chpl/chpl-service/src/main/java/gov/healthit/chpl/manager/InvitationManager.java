package gov.healthit.chpl.manager;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.dao.auth.InvitationDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dao.auth.UserPermissionDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.CreateUserRequest;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.auth.InvitationDTO;
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
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import lombok.extern.log4j.Log4j2;

@Service
@Log4j2
public class InvitationManager extends SecuredManager {
    private UserPermissionDAO userPermissionDao;
    private InvitationDAO invitationDao;
    private UserDAO userDao;
    private UserManager userManager;
    private UserPermissionsManager userPermissionsManager;
    private ActivityManager activityManager;
    private ResourcePermissions resourcePermissions;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings({"checkstyle:parameternumber"})
    public InvitationManager(UserPermissionDAO userPermissionDao, InvitationDAO invitationDao,
            UserDAO userDao, UserManager userManager, UserPermissionsManager userPermissionsManager,
            ActivityManager activityManager, ResourcePermissions resourcePermissions, ErrorMessageUtil msgUtil) {
        this.userPermissionDao = userPermissionDao;
        this.invitationDao = invitationDao;
        this.userDao = userDao;
        this.userManager = userManager;
        this.userPermissionsManager = userPermissionsManager;
        this.activityManager = activityManager;
        this.resourcePermissions = resourcePermissions;
        this.msgUtil = msgUtil;
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ADMIN)")
    public InvitationDTO inviteAdmin(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_ADMIN));
        Date now = new Date();
        dto.setInviteToken(Util.md5(emailAddress + now.getTime()));

        return createInvitation(dto);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ONC)")
    public InvitationDTO inviteOnc(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_ONC));
        Date now = new Date();
        dto.setInviteToken(Util.md5(emailAddress + now.getTime()));

        return createInvitation(dto);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_CMS)")
    public InvitationDTO inviteCms(String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_CMS_STAFF));
        Date now = new Date();
        dto.setInviteToken(Util.md5(emailAddress + now.getTime()));

        return createInvitation(dto);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ACB, #acbId)")
    public InvitationDTO inviteWithAcbAccess(String emailAddress, Long acbId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermissionObjectId(acbId);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_ACB));
        // could be multiple invitations for the same email so add the time to
        // make it unique
        Date currTime = new Date();
        dto.setInviteToken(Util.md5(emailAddress + currTime.getTime()));

        return createInvitation(dto);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ATL, #atlId)")
    public InvitationDTO inviteWithAtlAccess(String emailAddress, Long atlId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermissionObjectId(atlId);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_ATL));
        // could be multiple invitations for the same email so add the time to
        // make it unique
        Date currTime = new Date();
        dto.setInviteToken(Util.md5(emailAddress + currTime.getTime()));

        return createInvitation(dto);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_DEVELOPER, #developerId)")
    public InvitationDTO inviteWithDeveloperAccess(String emailAddress, Long developerId)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermissionObjectId(developerId);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_DEVELOPER));
        // could be multiple invitations for the same email so add the time to
        // make it unique
        Date currTime = new Date();
        dto.setInviteToken(Util.md5(emailAddress + currTime.getTime()));

        return createInvitation(dto);
    }

    private InvitationDTO createInvitation(InvitationDTO toCreate)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO createdInvitation = null;
        createdInvitation = invitationDao.create(toCreate);
        return createdInvitation;
    }

    @Transactional
    public InvitationDTO getByInvitationHash(String hash) {
        return invitationDao.getByInvitationToken(hash);
    }

    @Transactional
    public InvitationDTO getByConfirmationHash(String hash) {
        return invitationDao.getByConfirmationToken(hash);
    }

    @Transactional
    public InvitationDTO getById(Long id) throws UserRetrievalException {
        return invitationDao.getById(id);
    }

    @Transactional
    public UserDTO createUserFromInvitation(InvitationDTO invitation, CreateUserRequest user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException,
            UserCreationException, MultipleUserAccountsException {
        Authentication authenticator = AuthUtil.getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
        SecurityContextHolder.getContext().setAuthentication(authenticator);

        UserDTO existingUser = null;
        try {
            existingUser = userManager.getByNameOrEmail(user.getSubjectName());
        } catch (MultipleUserAccountsException ex) {
            //hitting this block means there are multiple users registered with this account.
            //don't let them make a new one!
            throw new InvalidArgumentsException(msgUtil.getMessage("user.accountAlreadyExists", user.getSubjectName()));
        }
        if (existingUser != null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("user.accountAlreadyExists", user.getSubjectName()));
        } else {
            try {
                existingUser = userManager.getByNameOrEmail(user.getEmail());
            } catch (MultipleUserAccountsException ex) {
                //hitting this block means there are multiple users registered with this account.
                //don't let them make a new one!
                throw new InvalidArgumentsException(msgUtil.getMessage("user.accountAlreadyExists", user.getEmail()));
            }
        }
        if (existingUser != null) {
            throw new InvalidArgumentsException(msgUtil.getMessage("user.accountAlreadyExists", user.getEmail()));
        }

        UserDTO toCreate = constructUser(invitation, user);
        UserDTO newUser = userManager.create(toCreate, user.getPassword());
        try {
            handleInvitation(invitation, newUser);

            // update invitation entity to change the hashes
            invitation.setCreatedUserId(newUser.getId());
            invitation.setInviteToken(null);
            Date now = new Date();
            invitation.setConfirmToken(Util.md5(invitation.getEmail() + now.getTime()));
            invitationDao.update(invitation);
            return newUser;
        } finally {
            SecurityContextHolder.getContext().setAuthentication(null);
        }

    }

    @Transactional
    public UserDTO confirmAccountEmail(InvitationDTO invitation) throws UserRetrievalException {
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

            String activityDescription = "User " + user.getSubjectName() + " was confirmed.";
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

    /**
     * A user can be added to additional ACBs if they are ROLE_ACB
     * or additional ATLs if they are ROLE_ATL.
     */
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).UPDATE_FROM_INVITATION, #userInvitation)")
    public UserDTO updateUserFromInvitation(UserInvitationDTO userInvitation)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException {
        User loggedInUser = gov.healthit.chpl.util.AuthUtil.getCurrentUser();

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
     *
     * @param invitation
     * @param user
     * @throws EntityRetrievalException
     * @throws InvalidArgumentsException
     * @throws UserRetrievalException
     */
    private void handleInvitation(InvitationDTO invitation, UserDTO user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException {
        CertificationBodyDTO userAcb = null;
        TestingLabDTO userAtl = null;
        DeveloperDTO userDeveloper = null;

        if (invitation.getPermission() != null && invitation.getPermission().getAuthority().equals(Authority.ROLE_ACB)
                && invitation.getPermissionObjectId() != null) {
            userAcb = resourcePermissions.getAcbIfPermissionById(invitation.getPermissionObjectId());
            if (userAcb == null) {
                throw new InvalidArgumentsException("Could not find ACB with id " + invitation.getPermissionObjectId());
            }
        } else if (invitation.getPermission() != null
                && invitation.getPermission().getAuthority().equals(Authority.ROLE_ATL)
                && invitation.getPermissionObjectId() != null) {
            userAtl = resourcePermissions.getAtlIfPermissionById(invitation.getPermissionObjectId());
            if (userAtl == null) {
                throw new InvalidArgumentsException(
                        "Could not find the testing lab with id " + invitation.getPermissionObjectId());
            }
        } else if (invitation.getPermission() != null
                && invitation.getPermission().getAuthority().equals(Authority.ROLE_DEVELOPER)
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

    private UserDTO constructUser(InvitationDTO invitation, CreateUserRequest user) {
        UserDTO userDto = new UserDTO();
        userDto.setTitle(user.getTitle());
        userDto.setSubjectName(user.getSubjectName());
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
