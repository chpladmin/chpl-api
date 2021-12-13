package gov.healthit.chpl.permissions.domains.certifiedproduct;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certifiedProductCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof CertifiedProductSearchDetails)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) obj;
            Long acbId = MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY);
            return isAcbValidForCurrentUser(acbId);
        } else {
            return false;
        }
    }

}
