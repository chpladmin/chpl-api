package gov.healthit.chpl.permissions.domains.activity;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.activity.CertificationBodyActivityMetadata;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("actionGetAcbActivityMetadataActionPermissions")
public class GetAcbActivityMetadataActionPermissions extends ActionPermissions {

    public GetAcbActivityMetadataActionPermissions() {
    }

    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof CertificationBodyActivityMetadata)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else {
            CertificationBodyActivityMetadata metadata = (CertificationBodyActivityMetadata) obj;
            return isAcbValidForCurrentUser(metadata.getAcbId());
        }
    }
}
