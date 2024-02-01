package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.auth.UserDAO;
import gov.healthit.chpl.domain.auth.Authority;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.exception.MultipleUserAccountsException;
import gov.healthit.chpl.exception.UserRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component(value = "userPermissionsImpersonateUserActionPermissions")
public class ImpersonateUserActionPermissions extends ActionPermissions {

    private UserDAO userDAO;

    @Autowired
    public ImpersonateUserActionPermissions(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    /**
     * Compare acting user's role with targeted user's role.
     * ROLE_ADMIN > all other roles
     * ROLE_ADMIN is not > ROLE_ADMIN
     * ROLE_ONC > all roles except ROLE_ADMIN and ROLE_ONC
     */
    @Override
    public boolean hasAccess(Object obj) {
        User target;
        if (obj instanceof Long) {
            target = getUserById((Long) obj);
        } else if (obj instanceof String) {
            target = getUserByEmail((String) obj);
        } else {
            LOGGER.error("Unable to get user by name or id %s", obj);
            return false;
        }

        if (getResourcePermissions().isUserRoleAdmin()) {
            //admin user can't impersonate another admin user
            if (target.getRole().equalsIgnoreCase(Authority.ROLE_ADMIN)) {
                return false;
            }
            return true;
        } else if (getResourcePermissions().isUserRoleOnc()) {
            //onc can't impersonate admin or onc
            if (target.getRole().equalsIgnoreCase(Authority.ROLE_ONC)
                    || target.getRole().equalsIgnoreCase(Authority.ROLE_ADMIN)) {
                return false;
            }
            return true;
        }
        //only admin or onc can impersonate
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

    private User getUserByEmail(String userEmail) {
        try {
            return userDAO.getByNameOrEmail(userEmail).toDomain();
        } catch (UserRetrievalException | MultipleUserAccountsException e) {
            LOGGER.error("Could not retrieve user with email: {}", userEmail, e);
            return null;
        }
    }
}
