package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    // Original security was:
    // @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC',
    // 'ROLE_INVITED_USER_CREATOR', "
    // + "'ROLE_ACB', 'ROLE_ATL', 'ROLE_USER_CREATOR')")
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin() || getResourcePermissions().isUserRoleAtlAdmin()
                || getResourcePermissions().isUserRoleUserCreator()
                || getResourcePermissions().isUserRoleInvitedUserCreator();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
