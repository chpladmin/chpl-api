package gov.healthit.chpl.permissions.domains.questionableActivity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("questionableActivityGetActionPermissions")
public class GetActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff();
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
