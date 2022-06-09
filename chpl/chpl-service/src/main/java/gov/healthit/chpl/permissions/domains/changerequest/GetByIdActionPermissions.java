package gov.healthit.chpl.permissions.domains.changerequest;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestGetByIdActionPermissions")
public class GetByIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof ChangeRequest)) {
                return false;
            } else if (getResourcePermissions().isUserRoleOnc() || getResourcePermissions().isUserRoleAdmin()) {
                return true;
            } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
                ChangeRequest cr = (ChangeRequest) obj;
                return isCurrentAcbUserAssociatedWithDeveloper(cr.getDeveloper().getId());
            } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
                ChangeRequest cr = (ChangeRequest) obj;
                return isDeveloperValidForCurrentUser(cr.getDeveloper().getId());
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }
}
