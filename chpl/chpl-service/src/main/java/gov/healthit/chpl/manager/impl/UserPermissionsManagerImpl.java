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

import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserDeveloperMapDAO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserDeveloperMapDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.manager.auth.UserManager;

@Component
public class UserPermissionsManagerImpl extends SecuredManager implements UserPermissionsManager {
    private static final Logger LOGGER = LogManager.getLogger(UserPermissionsManagerImpl.class);

    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserTestingLabMapDAO userTestingLabMapDAO;
    private UserDeveloperMapDAO userDeveloperMapDAO;
    private UserDAO userDAO;
    private UserManager userManager;
    private MutableAclService mutableAclService;

    @Autowired
    public UserPermissionsManagerImpl(final UserCertificationBodyMapDAO userCertificationBodyMapDAO,
            final UserTestingLabMapDAO userTestingLabMapDAO, final UserDeveloperMapDAO userDeveloperMapDAO,
            final UserDAO userDAO, final UserManager userManager, final MutableAclService mutableAclService) {

        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.userTestingLabMapDAO = userTestingLabMapDAO;
        this.userDeveloperMapDAO = userDeveloperMapDAO;
        this.userDAO = userDAO;
        this.userManager = userManager;
        this.mutableAclService = mutableAclService;
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
    public void deleteAcbPermission(final CertificationBodyDTO acb, final Long userId) throws EntityRetrievalException {
        // Get the UserCertBodyMapDTO
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(userId);

        if (dtos == null || dtos.size() == 0) {
            LOGGER.error("Could not locate the UserCertificationBodyMap object for Userid: " + userId + ", ACB: "
                    + acb.getId());
        }

        CollectionUtils.filter(dtos, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                return ((UserCertificationBodyMapDTO) object).getCertificationBody().getId().equals(acb.getId());
            }
        });

        for (UserCertificationBodyMapDTO dto : dtos) {
            userCertificationBodyMapDAO.delete(dto);
        }

        LOGGER.info("Deleted ACB: " + acb.getId() + " for user: " + userId);
        removeUserIfPermissionless(userId);
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
    public void deleteAtlPermission(final TestingLabDTO atl, final Long userId) throws EntityRetrievalException {
        // Get the UserTestingLabMapDTO
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(userId);

        if (dtos == null || dtos.size() == 0) {
            LOGGER.error(
                    "Could not locate the UserTestingLabMapDTO object for Userid: " + userId + ", ATL: " + atl.getId());
        }

        CollectionUtils.filter(dtos, new Predicate() {
            @Override
            public boolean evaluate(final Object object) {
                return ((UserTestingLabMapDTO) object).getTestingLab().getId().equals(atl.getId());
            }
        });

        for (UserTestingLabMapDTO dto : dtos) {
            userTestingLabMapDAO.delete(dto);
        }

        LOGGER.info("Deleted ATL: " + atl.getId() + " for user: " + userId);
        removeUserIfPermissionless(userId);
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
            LOGGER.error("Could not locate the UserDeveloperMap object for Userid: " + userId + ", Developer: " + developerId);
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

    private void removeUserIfPermissionless(final Long userId) {
        if (!doesUserHaveAnyPermissions(userId)) {
            UserDTO toDelete = new UserDTO();
            toDelete.setId(userId);
            try {
                // We can't call the user manager delete method here because
                // that only lets role onc and role admin remove the user.
                // We can't modify the permissions checker to confirm that
                // the user to be deleted has the same membership as the calling
                // user because at this point that membership has been removed.
                // Just delete the user here.

                // remove all ACLs for this user
                //should only be one - for themselves
                ObjectIdentity oid = new ObjectIdentityImpl(UserDTO.class, userId);
                mutableAclService.deleteAcl(oid, false);

                userDAO.delete(userId);
                LOGGER.info("User " + userId + " had no additional permissions. The user was deleted.");
            } catch (UserRetrievalException ex) {
                LOGGER.error("Could not delete the user " + userId, ex);
            }
        }
    }

    private boolean doesUserHaveAnyPermissions(final Long userId) {
        List<UserCertificationBodyMapDTO> acbPermissions = userCertificationBodyMapDAO.getByUserId(userId);
        List<UserTestingLabMapDTO> atlPermissions = userTestingLabMapDAO.getByUserId(userId);

        return (acbPermissions != null && acbPermissions.size() > 0)
                || (atlPermissions != null && atlPermissions.size() > 0);
    }
}
