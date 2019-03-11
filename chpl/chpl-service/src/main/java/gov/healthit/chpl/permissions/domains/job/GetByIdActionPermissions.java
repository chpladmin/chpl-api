package gov.healthit.chpl.permissions.domains.job;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("jobGetByIdActionPermissions")
public class GetByIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
