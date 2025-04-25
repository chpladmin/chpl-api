package gov.healthit.chpl.permissions.domains.secureduser;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("securedUserAddOrganizationToUserActionPermissions")
public class AddOrganizationToUserActionPermissions extends ActionPermissions {
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAcbAdmin() || getResourcePermissions().isUserRoleDeveloperAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        return false;
    }


}
