package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.entity.listing.pending.PendingCertifiedProductEntity;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductCreateOrReplaceActionPermissions")
public class CreateOrReplaceActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof PendingCertifiedProductEntity)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            PendingCertifiedProductEntity pcp = (PendingCertifiedProductEntity) obj;
            return isAcbValidForCurrentUser(pcp.getCertificationBodyId());
        } else {
            return false;
        }
    }
}
