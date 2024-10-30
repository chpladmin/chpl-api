package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (obj instanceof UserDTO) {
            UserDTO user = (UserDTO) obj;
            return getResourcePermissions().hasPermissionOnUser(user.toDomain());
        } else if (obj instanceof User) {
            User user = (User) obj;
            return getResourcePermissions().hasPermissionOnUser(user);
        }
        return false;
    }
}
