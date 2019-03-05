package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetUserActivityActionPermissions")
public class GetUserActivityActionPermissions extends ActionPermissions {

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
