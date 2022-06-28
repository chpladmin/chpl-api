package gov.healthit.chpl.permissions.domains.changerequest;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("changeRequestSearchActionPermissions")
public class SearchActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin() || getResourcePermissions().isUserRoleDeveloperAdmin();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccess(Object obj) {
        return false;
    }

}
