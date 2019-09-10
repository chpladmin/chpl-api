package gov.healthit.chpl.permissions.domains.changerequest;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleDeveloperAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
