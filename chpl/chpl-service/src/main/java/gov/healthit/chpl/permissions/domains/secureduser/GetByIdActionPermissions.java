package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserGetByIdActionPermissions")
public class GetByIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (obj instanceof Long) {
            Long userId = (Long) obj;
            return getResourcePermissions().hasPermissionOnUser(userId);
        } else if (obj instanceof UserDTO) {
            UserDTO user = (UserDTO) obj;
            return getResourcePermissions().hasPermissionOnUser(user);
        }
        return false;
    }

}
