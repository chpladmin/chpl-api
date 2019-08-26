package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.QuarterlyReportActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("activityGetQuarterlyReportActivityMetadataActionPermissions")
public class GetQuarterlyReportActivityMetadataActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof QuarterlyReportActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            QuarterlyReportActivityMetadata activity = (QuarterlyReportActivityMetadata) obj;
            return isAcbValidForCurrentUser(activity.getAcb().getId());
        } else {
            return false;
        }
    }

}
