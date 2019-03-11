package gov.healthit.chpl.permissions.domains.productversion;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("productVersionCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
