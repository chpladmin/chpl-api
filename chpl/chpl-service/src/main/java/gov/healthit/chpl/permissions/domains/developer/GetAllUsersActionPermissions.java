package gov.healthit.chpl.permissions.domains.developer;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("developerGetAllUsersActionPermissions")
public class GetAllUsersActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        Long devId = null;
        if (obj instanceof Long) {
            devId = (Long) obj;
        } else if (obj instanceof Developer) {
            devId = ((Developer) obj).getId();
        }
        if (devId == null) {
            return false;
        }

        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            boolean hasPermissionOnDev = false;
            for (Developer dev : getResourcePermissions().getAllDevelopersForCurrentUser()) {
                if (dev.getId().equals(devId)) {
                    hasPermissionOnDev = true;
                }
            }
            return hasPermissionOnDev;
        }
        return false;
    }

}
