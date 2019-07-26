package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.ComplaintActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("activityGetComplaintActivityMetadataActionPermissions")
public class GetComplaintActivityMetadataActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ComplaintActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ComplaintActivityMetadata activity = (ComplaintActivityMetadata) obj;
            return isAcbValidForCurrentUser(activity.getCertificationBody().getId());
        } else {
            return false;
        }
    }

}
