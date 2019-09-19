package gov.healthit.chpl.permissions.domains.changerequest;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        try {
            if (!(obj instanceof ChangeRequest)) {
                return false;
            } else if (getResourcePermissions().isUserRoleDeveloperAdmin()) {
                ChangeRequest cr = (ChangeRequest) obj;
                return isDeveloperValidForCurrentUser(cr.getDeveloper().getDeveloperId());
            } else {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

}
