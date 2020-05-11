package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Component("securedUserGetByUserNameActionPermisions")
@Log4j2
public class GetByUserNameActionPermissions extends ActionPermissions {

    private UserDAO userDAO;

    @Autowired
    public GetByUserNameActionPermissions(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public boolean hasAccess() {
        return !getResourcePermissions().isUserAnonymous();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        try {
            if (obj instanceof UserDTO) {
                return doesCurrentUserHavePermissionToSubjectUser((UserDTO) obj);
            } else if (obj instanceof User) {
                return doesCurrentUserHavePermissionToSubjectUser(getUserDTO((User) obj));
            } else {
                return false;
            }
        } catch (UserRetrievalException e) {
            LOGGER.error("Error retrieving user from DB. " + e.getMessage(), e);
            return false;
        }
    }

    private UserDTO getUserDTO(User user) throws UserRetrievalException {
        return userDAO.getById(user.getUserId());
    }

    private boolean doesCurrentUserHavePermissionToSubjectUser(UserDTO user) {
        return getResourcePermissions().isUserRoleUserAuthenticator()
                || getResourcePermissions().isUserRoleInvitedUserCreator()
                || getResourcePermissions().hasPermissionOnUser(user);
    }
}
