package gov.healthit.chpl.permissions.domains.qmsStandard;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("qmsStandardUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return  getResourcePermissions().isUserRoleAdmin()
            || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        // Not Used
        return false;
    }
}
