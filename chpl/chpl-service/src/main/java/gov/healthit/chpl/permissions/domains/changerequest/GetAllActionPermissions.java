package gov.healthit.chpl.permissions.domains.changerequest;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestGetAllActionPermissions")
public class GetAllActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin() || getResourcePermissions().isUserRoleDeveloperAdmin();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ChangeRequest)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ChangeRequest cr = (ChangeRequest) obj;
            return isCurrentAcbUserAssociatedWithDeveloper(cr.getDeveloper().getDeveloperId());
        } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
            ChangeRequest cr = (ChangeRequest) obj;
            return isDeveloperValidForCurrentUser(cr.getDeveloper().getDeveloperId());
        } else {
            return false;
        }
    }

}
