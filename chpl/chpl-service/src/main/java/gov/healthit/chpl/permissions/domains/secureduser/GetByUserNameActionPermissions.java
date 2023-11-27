package gov.healthit.chpl.permissions.domains.secureduser;

import java.util.UUID;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
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
    private FF4j ff4j;

    @Autowired
    public GetByUserNameActionPermissions(UserDAO userDAO, FF4j ff4j) {
        this.userDAO = userDAO;
        this.ff4j = ff4j;
    }

    @Override
    public boolean hasAccess() {
        return !getResourcePermissions().isUserAnonymous();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (obj == null) {
            return true;
        }

        try {
            if (ff4j.check(FeatureList.SSO)) {
                if (obj instanceof User) {
                    return doesCurrentUserHavePermissionToSubjectUser(((User) obj).getUserSsoId());
                } else {
                    return false;
                }
            } else {
                if (obj instanceof UserDTO) {
                    return doesCurrentUserHavePermissionToSubjectUser((UserDTO) obj);
                } else if (obj instanceof User) {
                    return doesCurrentUserHavePermissionToSubjectUser(getUserDTO((User) obj));
                } else {
                    return false;
                }
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

    private boolean doesCurrentUserHavePermissionToSubjectUser(UUID ssoUserId) {
        return getResourcePermissions().isUserRoleUserAuthenticator()
                || getResourcePermissions().isUserRoleInvitedUserCreator()
                || getResourcePermissions().hasPermissionOnUser(ssoUserId);
    }
}
