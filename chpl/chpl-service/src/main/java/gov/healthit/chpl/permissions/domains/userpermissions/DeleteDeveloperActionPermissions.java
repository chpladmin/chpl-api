package gov.healthit.chpl.permissions.domains.userpermissions;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsDeleteDeveloperActionPermissions")
public class DeleteDeveloperActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        Long developerId = null;
        if (obj instanceof DeveloperDTO) {
            developerId = ((DeveloperDTO) obj).getId();
        } else if (obj instanceof Long) {
            developerId = (Long) obj;
        }

        if (developerId == null) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            return isDeveloperValidForCurrentUser(developerId);
        } else {
            return false;
        }
    }

}
