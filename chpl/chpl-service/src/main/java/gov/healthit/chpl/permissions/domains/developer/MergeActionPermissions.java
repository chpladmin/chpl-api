package gov.healthit.chpl.permissions.domains.developer;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("developerMergeActionPermissions")
public class MergeActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            // Need to make sure all developers to be merged are active
            List<Long> developerIds = (List<Long>) obj;
            for (Long developerId : developerIds) {
                if (!getResourcePermissions().isDeveloperActive(developerId)) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
