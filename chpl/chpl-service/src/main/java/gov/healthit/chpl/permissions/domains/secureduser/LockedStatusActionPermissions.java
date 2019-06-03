package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserLockStatusActionPermissions")
public class LockedStatusActionPermissions extends ActionPermissions {

    // Original Security:
    // @PreAuthorize("hasRole('ROLE_USER_AUTHENTICATOR')")
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleUserAuthenticator();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        // TODO Auto-generated method stub
        return false;
    }

}
