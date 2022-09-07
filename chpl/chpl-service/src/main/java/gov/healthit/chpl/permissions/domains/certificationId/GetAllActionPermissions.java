package gov.healthit.chpl.permissions.domains.certificationId;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certificationIdsGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff()
                || getResourcePermissions().isUserRoleCmsStaff();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }
}
