package gov.healthit.chpl.permissions.domains.userpermissions;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsDeleteAtlActionPermissions")
public class DeleteAtlActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof TestingLabDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAtlAdmin()) {
            TestingLabDTO atl = (TestingLabDTO) obj;
            return isAtlValidForCurrentUser(atl.getId());
        } else {
            return false;
        }
    }

}
