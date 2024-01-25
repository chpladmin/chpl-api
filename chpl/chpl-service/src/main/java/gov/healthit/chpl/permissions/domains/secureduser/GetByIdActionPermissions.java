package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("securedUserGetByIdActionPermissions")
public class GetByIdActionPermissions extends ActionPermissions {

    private UserDAO userDAO;

    @Autowired
    public GetByIdActionPermissions(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (obj instanceof Long) {
            Long userId = (Long) obj;
            return getResourcePermissions().hasPermissionOnUser(getUserById(userId));
        } else if (obj instanceof UserDTO) {
            UserDTO user = (UserDTO) obj;
            return getResourcePermissions().hasPermissionOnUser(user.toDomain());
        }
        return false;
    }

    private User getUserById(Long userId) {
        try {
            return userDAO.getById(userId).toDomain();
        } catch (UserRetrievalException e) {
            LOGGER.error("Could not retrieve user with ID: {}", userId, e);
            return null;
        }
    }
}

