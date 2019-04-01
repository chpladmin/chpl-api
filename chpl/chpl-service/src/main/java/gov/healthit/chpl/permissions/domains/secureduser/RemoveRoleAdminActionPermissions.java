package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserRemoveRoleAdminActionPermissions")
public class RemoveRoleAdminActionPermissions extends ActionPermissions {

    // Original Security:
    // @PreAuthorize("hasRole('ROLE_ADMIN')")
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }
}
