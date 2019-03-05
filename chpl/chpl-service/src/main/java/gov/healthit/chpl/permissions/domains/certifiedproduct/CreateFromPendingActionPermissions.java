package gov.healthit.chpl.permissions.domains.certifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certifiedProductCreateFromPendingActionPermissions")
public class CreateFromPendingActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long acbId = (Long) obj;
            return isAcbValidForCurrentUser(acbId);
        } else {
            return false;
        }
    }

}
