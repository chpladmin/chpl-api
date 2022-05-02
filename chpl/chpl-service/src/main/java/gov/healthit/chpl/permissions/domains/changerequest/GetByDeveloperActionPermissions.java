package gov.healthit.chpl.permissions.domains.changerequest;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestGetByDeveloperActionPermissions")
public class GetByDeveloperActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin() || getResourcePermissions().isUserRoleDeveloperAdmin();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long developerId = (Long) obj;
            return isCurrentAcbUserAssociatedWithDeveloper(developerId);
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            Long developerId = (Long) obj;
            return isDeveloperValidForCurrentUser(developerId);
        } else {
            return false;
        }
    }

}
