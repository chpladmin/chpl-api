package gov.healthit.chpl.permissions.domains.userpermissions;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "userPermissionsAddDeveloperActionPermissions")
public class AddDeveloperActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof Developer)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin()
                || getResourcePermissions().isUserRoleInvitedUserCreator()) {
            return true;
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            Developer developer = (Developer) obj;
            return isDeveloperValidForCurrentUser(developer.getDeveloperId());
        } else {
            return false;
        }
    }

}
