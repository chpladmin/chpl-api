package gov.healthit.chpl.permissions.domains.scheduler;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component(value = "schedulerGetAllSystemTriggersActionPermissions")
public class GetAllSystemTriggersActionPermissions extends ActionPermissions {
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }
}
