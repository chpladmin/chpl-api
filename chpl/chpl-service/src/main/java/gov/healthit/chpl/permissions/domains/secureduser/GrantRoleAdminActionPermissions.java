package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserGrantRoleAdminActionPermissions")
public class GrantRoleAdminActionPermissions extends ActionPermissions {

    // Original Security:
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_INVITED_USER_CREATOR')")
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleInvitedUserCreator();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
