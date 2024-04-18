package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.auth.user.AuthenticationSystem;
import gov.healthit.chpl.domain.auth.User;
import gov.healthit.chpl.dto.auth.UserDTO;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public GetAllActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory) {
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

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
            //Need to use the 'correct' resource permissions based on the User object
            if (user.getCognitoId() != null) {
                return resourcePermissionsFactory.get(AuthenticationSystem.COGNTIO).hasPermissionOnUser(user);
            } else if (user.getUserId() != null) {
                return resourcePermissionsFactory.get(AuthenticationSystem.CHPL).hasPermissionOnUser(user);
            }
        }
        return false;
    }
}
