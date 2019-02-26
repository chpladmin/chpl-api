package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

/**
 * Permission for impersonating another user.
 * @author alarned
 *
 */
@Component(value = "userPermissionsImpersonateUserActionPermissions")
public class ImpersonateUserActionPermissions extends ActionPermissions {
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        return false;
    }
}
