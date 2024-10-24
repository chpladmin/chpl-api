package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleUserCreator()
                || getResourcePermissions().isUserRoleInvitedUserCreator();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
