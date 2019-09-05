package gov.healthit.chpl.permissions.domains.activity.changerequest;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.ChangeRequestDAO;
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
        if (!(obj instanceof ChangeRequestDAO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            ChangeRequest cr = (ChangeRequest) obj;
            return isDeveloperValidForCurrentUser(cr.getDeveloper().getDeveloperId());
        }
    }
}
