package gov.healthit.chpl.permissions.domains.surveillance;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceActivityReportActionPermission")
public class ActivityReportActionPermission extends ActionPermissions {

    @Override
    public boolean hasAccess() {
       return getResourcePermissions().isUserRoleAdmin()
               || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        // Not used
        return false;
    }
}
