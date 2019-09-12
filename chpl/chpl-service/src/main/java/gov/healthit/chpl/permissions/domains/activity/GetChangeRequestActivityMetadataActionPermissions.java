package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.domain.activity.ChangeRequestActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("activityGetChangeRequestActivityMetadataActionPermissions")
public class GetChangeRequestActivityMetadataActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof ChangeRequestActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            ChangeRequestActivityMetadata activity = (ChangeRequestActivityMetadata) obj;
            for (CertificationBody acb : activity.getCertificationBodies()) {
                if (isAcbValidForCurrentUser(acb.getId())) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

}
