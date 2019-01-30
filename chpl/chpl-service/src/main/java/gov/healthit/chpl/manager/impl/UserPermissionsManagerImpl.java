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

import gov.healthit.chpl.dao.UserCertificationBodyMapDAO;
import gov.healthit.chpl.dto.CertificationBodyDTO;
import gov.healthit.chpl.dto.UserCertificationBodyMapDTO;
import gov.healthit.chpl.dto.UserDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.UserPermissionsManager;
import gov.healthit.chpl.permissions.Permissions;

@Component
public class UserPermissionsManagerImpl implements UserPermissionsManager {
    private static final Logger LOGGER = LogManager.getLogger(UserPermissionsManagerImpl.class);

    private Permissions permissions;
    private UserCertificationBodyMapDAO userCertificationBodyMapDAO;

    @Autowired
    public UserPermissionsManagerImpl(final Permissions permissions,
            final UserCertificationBodyMapDAO userCertificationBodyMapDAO) {

        this.permissions = permissions;
        this.userCertificationBodyMapDAO = userCertificationBodyMapDAO;
    }

    @Override
    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionDomainPermissions).ADD, #acb)")
    public void addPermission(CertificationBodyDTO acb, Long userId) throws EntityRetrievalException {
        UserCertificationBodyMapDTO dto = new UserCertificationBodyMapDTO();
        dto.setCertificationBody(acb);
        UserDTO user = new UserDTO();
        user.setUserId(userId);
        dto.setUser(user);

        userCertificationBodyMapDAO.create(dto);
    }

    @Transactional
    @PreAuthorize("@permissions.hasAccess(T(gov.healthit.chpl.permissions.Permissions).USER_PERMISSIONS, "
            + "T(gov.healthit.chpl.permissions.domains.UserPermissionDomainPermissions).DELETE, #acb)")
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

}
