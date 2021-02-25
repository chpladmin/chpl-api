package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserUpdatePasswordActionPermissions")
public class UpdatePasswordActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof UserDTO)) {
            return false;
        } else {
            return getResourcePermissions().isUserRoleAdmin()
                    || getResourcePermissions().isUserRoleOnc()
                    || getResourcePermissions().hasPermissionOnUser((UserDTO) obj);
        }
    }

}
