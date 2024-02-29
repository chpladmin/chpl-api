package gov.healthit.chpl.permissions.domains.codeset;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("codeSetUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return  getResourcePermissions().isUserRoleAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        // Not Used
        return false;
    }


}
