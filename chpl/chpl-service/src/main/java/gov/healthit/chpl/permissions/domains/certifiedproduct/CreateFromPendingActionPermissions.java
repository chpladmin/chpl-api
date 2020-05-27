package gov.healthit.chpl.permissions.domains.certifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certifiedProductCreateFromPendingActionPermissions")
public class CreateFromPendingActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof PendingCertifiedProductDTO)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            PendingCertifiedProductDTO pendingListing = (PendingCertifiedProductDTO) obj;
            Long acbId = pendingListing.getCertificationBodyId();
            return isAcbValidForCurrentUser(acbId);
        } else {
            return false;
        }
    }

}
