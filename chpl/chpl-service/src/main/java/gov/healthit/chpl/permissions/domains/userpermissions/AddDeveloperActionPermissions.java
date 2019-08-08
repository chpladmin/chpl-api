package gov.healthit.chpl.permissions.domains.userpermissions;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsAddDeveloperActionPermissions")
public class AddDeveloperActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof DeveloperDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc() || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleInvitedUserCreator()) {
            return true;
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            DeveloperDTO developer = (DeveloperDTO) obj;
            return isDeveloperValidForCurrentUser(developer.getId());
        } else {
            return false;
        }
    }

}
