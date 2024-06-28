package gov.healthit.chpl.permissions.domains.developer;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("developerUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Developer)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            // The user can only update if the developer is currently active
            // Need to get the current status for the developer from the DB...
            Developer originalDeveloper = (Developer) obj;
            return getResourcePermissions().isDeveloperNotBannedOrSuspended(originalDeveloper.getId());
        } else {
            return false;
        }
    }

}
