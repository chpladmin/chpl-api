package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserGetByPermissionActionPermissions")
public class GetByPermissionActionPermissions extends ActionPermissions {

    // Original security:
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC', 'ROLE_ACB',
    // 'ROLE_ATL', 'ROLE_CMS_STAFF')")
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin() || getResourcePermissions().isUserRoleAtlAdmin()
                || getResourcePermissions().isUserRoleCmsStaff();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
