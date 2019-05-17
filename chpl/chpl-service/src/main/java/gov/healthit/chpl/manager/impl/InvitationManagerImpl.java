package gov.healthit.chpl.manager.impl;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.auth.InvitationDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.dto.auth.UserInvitationDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.exception.UserCreationException;
import gov.healthit.chpl.exception.UserPermissionRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.InvitationManager;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.manager.auth.UserManager;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.AuthUtil;
import gov.healthit.chpl.util.Util;

@Service
public class InvitationManagerImpl extends SecuredManager implements InvitationManager {

    private static final Logger LOGGER = LogManager.getLogger(InvitationManagerImpl.class);

    @Autowired
    private UserPermissionDAO userPermissionDao;

    @Autowired
    private InvitationDAO invitationDao;

    @Autowired
    private UserDAO userDao;

    @Autowired
    private UserManager userManager;

    @Autowired
    private UserPermissionsManager userPermissionsManager;

    @Autowired
    private ActivityManager activityManager;

    @Autowired
    private ResourcePermissions resourcePermissions;

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ADMIN)")
    public InvitationDTO inviteAdmin(final String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_ADMIN));
        Date now = new Date();
        dto.setInviteToken(Util.md5(emailAddress + now.getTime()));

        return createInvitation(dto);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ONC)")
    public InvitationDTO inviteOnc(final String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_ONC));
        Date now = new Date();
        dto.setInviteToken(Util.md5(emailAddress + now.getTime()));

        return createInvitation(dto);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_CMS)")
    public InvitationDTO inviteCms(final String emailAddress)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO dto = new InvitationDTO();
        dto.setEmail(emailAddress);
        dto.setPermission(userPermissionDao.getPermissionFromAuthority(Authority.ROLE_CMS_STAFF));
        Date now = new Date();
        dto.setInviteToken(Util.md5(emailAddress + now.getTime()));

        return createInvitation(dto);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ACB, #acbId)")
    public InvitationDTO inviteWithAcbAccess(final String emailAddress, final Long acbId)
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

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).INVITE_ATL, #atlId)")
    public InvitationDTO inviteWithAtlAccess(final String emailAddress, final Long atlId)
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

    private InvitationDTO createInvitation(final InvitationDTO toCreate)
            throws UserCreationException, UserRetrievalException, UserPermissionRetrievalException {
        InvitationDTO createdInvitation = null;
        createdInvitation = invitationDao.create(toCreate);
        return createdInvitation;
    }

    @Override
    @Transactional
    public InvitationDTO getByInvitationHash(final String hash) {
        return invitationDao.getByInvitationToken(hash);
    }

    @Override
    @Transactional
    public InvitationDTO getByConfirmationHash(final String hash) {
        return invitationDao.getByConfirmationToken(hash);
    }

    @Override
    @Transactional
    public InvitationDTO getById(final Long id) throws UserRetrievalException {
        return invitationDao.getById(id);
    }

    @Override
    @Transactional
    public UserDTO createUserFromInvitation(final InvitationDTO invitation, final CreateUserRequest user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException, UserCreationException {
        Authentication authenticator = AuthUtil.getInvitedUserAuthenticator(invitation.getLastModifiedUserId());
        SecurityContextHolder.getContext().setAuthentication(authenticator);

        // create the user
        UserDTO newUser = null;

        try {
            newUser = userManager.getByName(user.getSubjectName());
            if (newUser == null) {
                UserDTO toCreate = constructUser(invitation, user);
                newUser = userManager.create(toCreate, user.getPassword());
            } else {
                throw new InvalidArgumentsException(
                        "A user with the name " + user.getSubjectName() + " already exists.");
            }
        } catch (UserRetrievalException ex) {
            UserDTO toCreate = constructUser(invitation, user);
            newUser = userManager.create(toCreate, user.getPassword());
        }

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

    @Override
    @Transactional
    public UserDTO confirmAccountEmail(final InvitationDTO invitation) throws UserRetrievalException {
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
    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).INVITATION, "
            + "T(gov.healthit.chpl.permissions.domains.InvitationDomainPermissions).UPDATE_FROM_INVITATION, #userInvitation)")
    public UserDTO updateUserFromInvitation(final UserInvitationDTO userInvitation)
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
     * Adds the invited user (who has now created an account) to any ACBs or ATLs
     * that the invitation specifies they should have access to.
     * Also could be an existing user getting ACBs or ATLs added to their account.
     * The securitycontext must have a valid authentication specified when this is called
     *
     * @param invitation
     * @param user
     * @throws EntityRetrievalException
     * @throws InvalidArgumentsException
     * @throws UserRetrievalException
     */
    private void handleInvitation(final InvitationDTO invitation, final UserDTO user)
            throws EntityRetrievalException, InvalidArgumentsException, UserRetrievalException {
        CertificationBodyDTO userAcb = null;
        TestingLabDTO userAtl = null;

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
        }

        // give them access to the invited acb or atl
        if (userAcb != null) {
            userPermissionsManager.addAcbPermission(userAcb, user.getId());
        } else if (userAtl != null) {
            userPermissionsManager.addAtlPermission(userAtl, user.getId());
        }
    }

    private UserDTO constructUser(final InvitationDTO invitation, final CreateUserRequest user) {
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
