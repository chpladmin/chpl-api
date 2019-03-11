package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
