package gov.healthit.chpl.permissions.domains.conformanceMethod;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("conformanceMethodDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {

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
