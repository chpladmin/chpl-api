package gov.healthit.chpl.manager.impl;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dao.UserTestingLabMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserTestingLabMapDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.permissions.Permissions;

@Component
public class UserPermissionsManagerImpl implements UserPermissionsManager {
    private static final Logger LOGGER = LogManager.getLogger(UserPermissionsManagerImpl.class);

    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserTestingLabMapDAO userTestingLabMapDAO;
    private UserDAO userDAO;
    private Permissions permissions;

    @Autowired
    public UserPermissionsManagerImpl(final UserCertificationBodyMapDAO userCertificationBodyMapDAO,
            final UserTestingLabMapDAO userTestingLabMapDAO, final UserDAO userDAO, final Permissions permissions) {

        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.userTestingLabMapDAO = userTestingLabMapDAO;
        this.userDAO = userDAO;
        this.permissions = permissions;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).ADD_ACB, #acb)")
    public void addAcbPermission(CertificationBodyDTO acb, Long userId)
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
            public boolean evaluate(Object object) {
                return ((UserCertificationBodyMapDTO) object).getCertificationBody().getId().equals(acb.getId());
            }
        });

        for (UserCertificationBodyMapDTO dto : dtos) {
            userCertificationBodyMapDAO.delete(dto);
        }

        LOGGER.debug("Deleted ACB: " + acb.getId() + " for user: " + userId);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).DELETE_ALL_ACBS_FOR_USER)")
    public void deleteAllAcbPermissionsForUser(final Long userId) throws EntityRetrievalException {
        // Get the UserCertBodyMapDTO
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(userId);

        if (dtos != null) {
            for (UserCertificationBodyMapDTO dto : dtos) {
                userCertificationBodyMapDAO.delete(dto);
                LOGGER.debug("Deleted ACB: " + dto.getCertificationBody().getId() + " for user: " + userId);
            }
        }
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).ADD_ATL, #atl)")
    public void addAtlPermission(TestingLabDTO atl, Long userId)
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
            public boolean evaluate(Object object) {
                return ((UserTestingLabMapDTO) object).getTestingLab().getId().equals(atl.getId());
            }
        });

        for (UserTestingLabMapDTO dto : dtos) {
            userTestingLabMapDAO.delete(dto);
        }

        LOGGER.debug("Deleted ATL: " + atl.getId() + " for user: " + userId);
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionsDomainPermissions).DELETE_ALL_ATLS_FOR_USER)")
    public void deleteAllAtlPermissionsForUser(final Long userId) throws EntityRetrievalException {
        // Get the UserTestingLabMapDTO
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(userId);

        if (dtos != null) {
            for (UserTestingLabMapDTO dto : dtos) {
                userTestingLabMapDAO.delete(dto);
                LOGGER.debug("Deleted ATL: " + dto.getTestingLab().getId() + " for user: " + userId);
            }
        }
    }

    private Boolean doesUserCertificationBodyMapExist(Long acbId, Long userId) {
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(userId);

        if (dtos == null || dtos.size() == 0) {
            LOGGER.error(
                    "Could not locate the UserCertificationBodyMap object for Userid: " + userId + ", ACB: " + acbId);
        }

        CollectionUtils.filter(dtos, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((UserCertificationBodyMapDTO) object).getCertificationBody().getId().equals(acbId)
                        && ((UserCertificationBodyMapDTO) object).getUser().getId().equals(userId);
            }
        });

        return dtos.size() > 0;
    }

    private Boolean doesUserTestingLabMapExist(Long atlId, Long userId) {
        List<UserTestingLabMapDTO> dtos = userTestingLabMapDAO.getByUserId(userId);

        if (dtos == null || dtos.size() == 0) {
            LOGGER.error("Could not locate the UserTestingLabyMap object for Userid: " + userId + ", ATL: " + atlId);
        }

        CollectionUtils.filter(dtos, new Predicate() {
            @Override
            public boolean evaluate(Object object) {
                return ((UserTestingLabMapDTO) object).getTestingLab().getId().equals(atlId)
                        && ((UserTestingLabMapDTO) object).getUser().getId().equals(userId);
            }
        });

        return dtos.size() > 0;
    }

}
