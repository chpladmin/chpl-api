package gov.healthit.chpl.permissions.domains.developer;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.DeveloperDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("developerSplitActionPermissions")
public class SplitActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof DeveloperDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            DeveloperDTO developer = (DeveloperDTO) obj;
            if (getResourcePermissions().isDeveloperActive(developer.getId())) {
                // ACB can only split developer if original developer is active and all listings owned by the developer
                // belong to the user's ACB
                return doesCurrentUserHaveAccessToAllOfDevelopersListings(developer.getId());
            } else {
                // ACB can never split developer if original developer is not active
                return false;
            }
        } else {
            return false;
        }
    }
}
