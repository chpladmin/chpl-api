package gov.healthit.chpl.permissions.domains.testinglab;

import gov.healthit.chpl.dto.TestingLabDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

public class AllUsersOnAtlActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof TestingLabDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            TestingLabDTO atl = (TestingLabDTO) obj;
            return isAtlValidForCurrentUser(atl.getId());
        }
    }

}
