package gov.healthit.chpl.manager.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.auth.dao.UserDAO;
import gov.healthit.chpl.auth.dto.UserDTO;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.auth.user.UserRetrievalException;
import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.UserPermissionsManager;

@Component
public class UserPermissionsManagerImpl implements UserPermissionsManager {
    private static final Logger LOGGER = LogManager.getLogger(UserPermissionsManagerImpl.class);

    // private Permissions permissions;
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;
    private UserDAO userDAO;

    @Autowired
    public UserPermissionsManagerImpl(// final Permissions permissions,
            final UserCertificationBodyMapDAO userCertificationBodyMapDAO, final UserDAO userDAO) {

        // this.permissions = permissions;
        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
        this.userDAO = userDAO;
    }

    @Override
    @Transactional
    // @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS,
    // "
    // +
    // "T(gov.healthit.chpl.permissions.domains.UserPermissionDomainPermissions).ADD,
    // #acb)")
    public void addPermission(CertificationBodyDTO acb, Long userId)
            throws EntityRetrievalException, UserRetrievalException {
        UserCertificationBodyMapDTO dto = new UserCertificationBodyMapDTO();
        dto.setCertificationBody(acb);
        UserDTO user = userDAO.getById(userId);
        dto.setUser(user);

        userCertificationBodyMapDAO.create(dto);
    }

    @Transactional
    // @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS,
    // "
    // +
    // "T(gov.healthit.chpl.permissions.domains.UserPermissionDomainPermissions).DELETE,
    // #acb)")
    public void deletePermission(final CertificationBodyDTO acb, final Long userId) throws EntityRetrievalException {
        // Get the UserCertBodyMapDTO
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(userId);

        if (dtos == null || dtos.size() == 0) {
            // TODO: throw an exception
            // throw exception...
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
    @Transactional(readOnly = true)
    public List<CertificationBodyDTO> getAllAcbsForCurrentUser() {
        User user = Util.getCurrentUser();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByUserId(user.getId());

        List<CertificationBodyDTO> acbs = new ArrayList<CertificationBodyDTO>();
        for (UserCertificationBodyMapDTO dto : dtos) {
            acbs.add(dto.getCertificationBody());
        }
        return acbs;
    }

    @Transactional
    // @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).CERTIFICATION_BODY,
    // "
    // +
    // "T(gov.healthit.chpl.permissions.domains.CertificationBodyDomainPermissions).USERS_BY_ACB,
    // #acb)")
    public List<UserDTO> getAllUsersOnAcb(final CertificationBodyDTO acb) {
        List<UserDTO> userDtos = new ArrayList<UserDTO>();
        List<UserCertificationBodyMapDTO> dtos = userCertificationBodyMapDAO.getByAcbId(acb.getId());

        for (UserCertificationBodyMapDTO dto : dtos) {
            userDtos.add(dto.getUser());
        }

        return userDtos;
    }

}
