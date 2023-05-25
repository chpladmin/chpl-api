package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserGetByPermissionActionPermissions")
public class GetByPermissionActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff() || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleCmsStaff();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
