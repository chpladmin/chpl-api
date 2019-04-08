package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductMetadataDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductGetAllMetadataActionPermissions")
public class GetAllMetadataActionPermissions extends ActionPermissions {
    @Override
    public boolean hasAccess() {
        return getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleAcbAdmin();
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof PendingCertifiedProductMetadataDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            PendingCertifiedProductMetadataDTO pcpMetadata = (PendingCertifiedProductMetadataDTO) obj;
            return isAcbValidForCurrentUser(pcpMetadata.getAcbId());
        } else {
            return false;
        }
    }

}
