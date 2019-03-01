package gov.healthit.chpl.permissions.domains.userpermissions;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsDeleteAllAcbPermissionsForUserActionPermissions")
public class DeleteAllAcbPermissionsForUserActionPermissions extends ActionPermissions {
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }
}
