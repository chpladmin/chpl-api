package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("activityGetUserMaintenanceMetadataActionPermissions")
public class GetUserMaintenanceMetadataActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleAcbAdmin()) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean hasAccess(Object obj) {
        return false;
    }

}
