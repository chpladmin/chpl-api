package gov.healthit.chpl.permissions.domains.functionalitytested;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("functionalityTestedCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {
    @Override
    public boolean hasAccess() {
        return  getResourcePermissions().isUserRoleAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        // Not Used
        return false;
    }
}
