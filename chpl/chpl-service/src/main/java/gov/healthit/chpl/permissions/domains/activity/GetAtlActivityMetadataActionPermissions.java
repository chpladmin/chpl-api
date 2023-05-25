package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.TestingLabActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetAtlActivityMetadataActionPermissions")
public class GetAtlActivityMetadataActionPermissions extends ActionPermissions {

    public GetAtlActivityMetadataActionPermissions() {
    }

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff() ;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof TestingLabActivityMetadata)) {
            return false;
        }
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleOncStaff();
    }
}
