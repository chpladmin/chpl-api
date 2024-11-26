package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("activityGetUserMaintenanceMetadataActionPermissions")
public class GetUserMaintenanceMetadataActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc();
    }

    @Override
    @Transactional
    public boolean hasAccess(Object obj) {
        return false;
    }
}
