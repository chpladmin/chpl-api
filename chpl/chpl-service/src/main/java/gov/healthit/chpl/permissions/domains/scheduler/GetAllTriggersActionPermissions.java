package gov.healthit.chpl.permissions.domains.scheduler;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "schedulerGetAllTriggersActionPermissions")
public class GetAllTriggersActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }
}
