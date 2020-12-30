package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.acls.domain.ObjectIdentityImpl;
import org.springframework.security.acls.model.MutableAclService;
import org.springframework.security.acls.model.ObjectIdentity;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.UserPermissionsManager;

@Component
public class UserPermissionsManagerImpl extends SecuredManager implements UserPermissionsManager {
    private static final Logger LOGGER = LogManager.getLogger(UserPermissionsManagerImpl.class);

    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserTestingLabMapDAO userTestingLabMapDAO;
    private UserDeveloperMapDAO userDeveloperMapDAO;
    private UserDAO userDAO;
    private MutableAclService mutableAclService;
    private ActivityManager activityManager;

    @Autowired
    public UserPermissionsManagerImpl(final UserCertificationBodyMapDAO userCertificationBodyMapDAO,
            final UserTestingLabMapDAO userTestingLabMapDAO, final UserDeveloperMapDAO userDeveloperMapDAO,
            final UserDAO userDAO, final MutableAclService mutableAclService, final ActivityManager activityManager) {

        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.userTestingLabMapDAO = userTestingLabMapDAO;
        this.userDeveloperMapDAO = userDeveloperMapDAO;
        this.userDAO = userDAO;
        this.mutableAclService = mutableAclService;
        this.activityManager = activityManager;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).ADD_ACB, #acb)")
    public void addAcbPermission(final CertificationBodyDTO acb, final Long userId)
            throws EntityRetrievalException, UserRetrievalException {

        if (doesUserCertificationBodyMapExist(acb.getId(), userId)) {
            LOGGER.info("User (" + userId + ") already has permission to ACB (" + acb.getId() + ").");
        } else {
            UserCertificationBodyMapDTO dto = new UserCertificationBodyMapDTO();
            dto.setCertificationBody(acb);
            UserDTO user = userDAO.getById(userId);
            dto.setUser(user);

            userCertificationBodyMapDAO.create(dto);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).DELETE_ACB, #acb)")
    public void deleteAcbPermission(final CertificationBodyDTO acb, final Long userId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        // Get the UserCertBodyMapDTO
        List<UserCertificationBodyMapDTO> userPermissions = userCertificationBodyMapDAO.getByUserId(userId);
        UserDTO originalUser = getUser(userId);

        if (userPermissions == null || userPermissions.size() == 0) {
            LOGGER.error("Could not locate the UserCertificationBodyMap object for Userid: " + userId + ", ACB: "
                    + acb.getId());
        }

        CollectionUtils.filter(userPermissions, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                return ((UserCertificationBodyMapDTO) object).getCertificationBody().getId().equals(acb.getId());
            }
        });

        if (userPermissions.size() > 0) {
            //remove the permission, only one can be getting removed per method call
            UserCertificationBodyMapDTO permissionToRemove = userPermissions.get(0);
            userCertificationBodyMapDAO.delete(permissionToRemove);

            if (!doesUserHaveAnyPermissions(userId)) {
                //if there are no additional permissions for this user
                //remove them and log a single activity
                removeUser(originalUser);
            } else {
                //user has additional permissions so log
                //the user update activity with one permission removal
                UserDTO updatedUser = getUser(userId);
                String message = "Removed " + permissionToRemove.getCertificationBody().getName() + " from "
                            + permissionToRemove.getUser().getUsername();
                activityManager.addActivity(ActivityConcept.USER, userId, message, originalUser, updatedUser);
            }
            LOGGER.info("Deleted ACB: " + acb.getId() + " for user: " + userId);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).ADD_ATL, #atl)")
    public void addAtlPermission(final TestingLabDTO atl, final Long userId)
            throws EntityRetrievalException, UserRetrievalException {

        if (doesUserTestingLabMapExist(atl.getId(), userId)) {
            LOGGER.info("User (" + userId + ") already has permission to ATL (" + atl.getId() + ").");
        } else {
            UserTestingLabMapDTO dto = new UserTestingLabMapDTO();
            dto.setTestingLab(atl);
            UserDTO user = userDAO.getById(userId);
            dto.setUser(user);

            userTestingLabMapDAO.create(dto);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).DELETE_ATL, #atl)")
    public void deleteAtlPermission(final TestingLabDTO atl, final Long userId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        // Get the UserTestingLabMapDTO
        List<UserTestingLabMapDTO> userPermissions = userTestingLabMapDAO.getByUserId(userId);
        UserDTO originalUser = getUser(userId);

        if (userPermissions == null || userPermissions.size() == 0) {
            LOGGER.error(
                    "Could not locate the UserTestingLabMapDTO object for Userid: " + userId + ", ATL: " + atl.getId());
        }

        CollectionUtils.filter(userPermissions, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                return ((UserTestingLabMapDTO) object).getTestingLab().getId().equals(atl.getId());
            }
        });

        if (userPermissions.size() > 0) {
            //remove the permission, only one can be getting removed per method call
            UserTestingLabMapDTO permissionToRemove = userPermissions.get(0);
            userTestingLabMapDAO.delete(permissionToRemove);

            if (!doesUserHaveAnyPermissions(userId)) {
                //if there are no additional permissions for this user
                //remove them and log a single activity
                removeUser(originalUser);
            } else {
                //user has additional permissions so log
                //the user update activity with one permission removal
                UserDTO updatedUser = getUser(userId);
                String message = "Removed " + permissionToRemove.getTestingLab().getName() + " from "
                            + permissionToRemove.getUser().getUsername();
                activityManager.addActivity(ActivityConcept.USER, userId, message, originalUser, updatedUser);
            }
            LOGGER.info("Deleted ATL: " + atl.getId() + " for user: " + userId);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).ADD_DEVELOPER, #developer)")
    public void addDeveloperPermission(final DeveloperDTO developer, final Long userId)
            throws EntityRetrievalException, UserRetrievalException {

        if (doesUserDeveloperMapExist(developer.getId(), userId)) {
            LOGGER.info("User (" + userId + ") already has permission to Developer (" + developer.getId() + ").");
        } else {
            UserDeveloperMapDTO dto = new UserDeveloperMapDTO();
            dto.setDeveloper(developer);
            UserDTO user = userDAO.getById(userId);
            dto.setUser(user);

            userDeveloperMapDAO.create(dto);
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).DELETE_DEVELOPER, #developerId)")
    public void deleteDeveloperPermission(final Long developerId, final Long userId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        List<UserDeveloperMapDTO> userPermissions = userDeveloperMapDAO.getByUserId(userId);
        UserDTO originalUser = getUser(userId);

        if (userPermissions == null || userPermissions.size() == 0) {
            LOGGER.error("Could not locate the UserDeveloperMapDTO object for Userid: " + userId + ", Developer: "
                    + developerId);
        }

        CollectionUtils.filter(userPermissions, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                return ((UserDeveloperMapDTO) object).getDeveloper().getId().equals(developerId);
            }
        });

        if (userPermissions.size() > 0) {
            //remove the permission, only one can be getting removed per method call
            UserDeveloperMapDTO permissionToRemove = userPermissions.get(0);
            userDeveloperMapDAO.delete(permissionToRemove);

            if (!doesUserHaveAnyPermissions(userId)) {
                //if there are no additional permissions for this user
                //remove them and log a single activity
                removeUser(originalUser);
            } else {
                //user has additional permissions so log
                //the user update activity with one permission removal
                UserDTO updatedUser = getUser(userId);
                String message = "Removed " + permissionToRemove.getDeveloper().getName() + " from "
                            + permissionToRemove.getUser().getUsername();
                activityManager.addActivity(ActivityConcept.USER, userId, message, originalUser, updatedUser);
            }
            LOGGER.info("Deleted Developer: " + permissionToRemove.getDeveloper().getName() + " for user: " + userId);
        }
    }

    private Boolean doesUserCertificationBodyMapExist(final Long acbId, final Long userId) {
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(userId);

        if (dtos == null || dtos.size() == 0) {
            LOGGER.error(
                    "Could not locate the UserCertificationBodyMap object for Userid: " + userId + ", ACB: " + acbId);
        }

        CollectionUtils.filter(dtos, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                return ((UserCertificationBodyMapDTO) object).getCertificationBody().getId().equals(acbId)
                        && ((UserCertificationBodyMapDTO) object).getUser().getId().equals(userId);
            }
        });

        return dtos.size() > 0;
    }

    private Boolean doesUserTestingLabMapExist(final Long atlId, final Long userId) {
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(userId);

        if (dtos == null || dtos.size() == 0) {
            LOGGER.error("Could not locate the UserTestingLabyMap object for Userid: " + userId + ", ATL: " + atlId);
        }

        CollectionUtils.filter(dtos, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                return ((UserTestingLabMapDTO) object).getTestingLab().getId().equals(atlId)
                        && ((UserTestingLabMapDTO) object).getUser().getId().equals(userId);
            }
        });

        return dtos.size() > 0;
    }

    private Boolean doesUserDeveloperMapExist(final Long developerId, final Long userId) {
        List<UserDeveloperMapDTO> dtos = userDeveloperMapDAO.getByUserId(userId);

        if (dtos == null || dtos.size() == 0) {
            LOGGER.error("Could not locate the UserDeveloperMap object for Userid: " + userId + ", Developer: "
                    + developerId);
        }

        CollectionUtils.filter(dtos, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                return ((UserDeveloperMapDTO) object).getDeveloper().getId().equals(developerId)
                        && ((UserDeveloperMapDTO) object).getUser().getId().equals(userId);
            }
        });

        return dtos.size() > 0;
    }

    private void removeUser(final UserDTO user) throws JsonProcessingException,
        EntityCreationException, EntityRetrievalException {
        try {
            // We can't call the user manager delete method here because
            // that only lets role onc and role admin remove the user.
            // We can't modify the permissions checker to confirm that
            // the user to be deleted has the same membership as the calling
            // user because at this point that membership has been removed.
            // Just delete the user here.

            // remove all ACLs for this user
            // should only be one - for themselves
            ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, user.getId());
            mutableAclService.deleteAcl(oid, false);

            userDAO.delete(user.getId());

            String message = "Deleted user " + user.getUsername();
            activityManager.addActivity(ActivityConcept.USER, user.getId(), message, user, null);
        } catch (UserRetrievalException ex) {
            LOGGER.error("Could not delete the user " + user, ex);
        }
    }

    private boolean doesUserHaveAnyPermissions(final Long userId) {
        List<UserCertificationBodyMapDTO> acbPermissions = userCertificationBodyMapDAO.getByUserId(userId);
        List<UserTestingLabMapDTO> atlPermissions = userTestingLabMapDAO.getByUserId(userId);
        List<UserDeveloperMapDTO> devPermissions = userDeveloperMapDAO.getByUserId(userId);

        return (acbPermissions != null && acbPermissions.size() > 0)
                || (atlPermissions != null && atlPermissions.size() > 0)
                || (devPermissions != null && devPermissions.size() > 0);
    }

    private UserDTO getUser(final Long userId) throws EntityRetrievalException {
        try {
            return userDAO.getById(userId);
        } catch (UserRetrievalException e) {
            throw new EntityRetrievalException(e);
        }
    }
}
