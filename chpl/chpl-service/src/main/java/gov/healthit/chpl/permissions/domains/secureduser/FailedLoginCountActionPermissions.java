package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserFailedLoginCountActionPermissions")
public class FailedLoginCountActionPermissions extends ActionPermissions {

    // Original Security:
    // @PreAuthorize("hasRole('ROLE_USER_AUTHENTICATOR')")
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleUserAuthenticator();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
