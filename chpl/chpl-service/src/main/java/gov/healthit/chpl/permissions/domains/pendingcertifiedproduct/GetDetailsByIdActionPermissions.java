package gov.healthit.chpl.permissions.domains.pendingcertifiedproduct;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.PendingCertifiedProductDetails;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("pendingCertifiedProductGetDetailsByIdActionPermissions")
public class GetDetailsByIdActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (obj == null || !(obj instanceof PendingCertifiedProductDetails)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            //admin can see any pending listing
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            //acb can see pending listing if it is
            //on their acb
            PendingCertifiedProductDetails pcp = (PendingCertifiedProductDetails) obj;
            if (pcp.getCertifyingBody() != null && pcp.getCertifyingBody().get("id") != null) {
                Long acbId = new Long(pcp.getCertifyingBody().get("id").toString());
                return isAcbValidForCurrentUser(acbId);
            }
        }
        return false;
    }

}
