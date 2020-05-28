package gov.healthit.chpl.permissions.domains.secureduser;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Component("securedUserUpdateActionPermissions")
@Log4j2
public class UpdateActionPermissions extends ActionPermissions {

    private UserDAO userDAO;

    @Autowired
    public UpdateActionPermissions(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (Objects.nonNull(obj) && ((obj instanceof UserDTO) || (obj instanceof User))) {

            UserDTO user = null;
            if (obj instanceof UserDTO) {
                user = (UserDTO) obj;
            } else if (obj instanceof User) {
                user = getUserDto(((User) obj).getUserId());
                if (Objects.isNull(user)) {
                    return false;
                }
            }
            return getResourcePermissions().hasPermissionOnUser(user);
        } else {
            return false;
        }
    }

    private UserDTO getUserDto(Long userId) {
        try {
            return userDAO.getById(userId);
        } catch (UserRetrievalException e) {
            LOGGER.error(String.format("Could not find user with Id: %s", userId));
            return null;
        }
    }
}
