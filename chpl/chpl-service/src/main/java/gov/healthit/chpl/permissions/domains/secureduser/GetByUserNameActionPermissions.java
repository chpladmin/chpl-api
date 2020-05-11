package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserGetByUserNameActionPermisions")
public class GetByUserNameActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return !getResourcePermissions().isUserAnonymous();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof UserDTO)) {
            return false;
        } else {
            UserDTO user = (UserDTO) obj;
            return getResourcePermissions().isUserRoleUserAuthenticator()
                    || getResourcePermissions().isUserRoleInvitedUserCreator()
                    || getResourcePermissions().hasPermissionOnUser(user);
        }
    }

}
