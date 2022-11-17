package gov.healthit.chpl.permissions.domains.ucdProcess;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("ucdProcessUpdateActionPermissions")
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
