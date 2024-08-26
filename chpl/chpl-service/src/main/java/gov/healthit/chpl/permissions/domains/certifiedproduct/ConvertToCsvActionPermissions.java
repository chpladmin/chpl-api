package gov.healthit.chpl.permissions.domains.certifiedproduct;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.CertificationStatusUtil;

@Component("certifiedProductConvertToCsvActionPermissions")
public class ConvertToCsvActionPermissions extends ActionPermissions {

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof CertifiedProductSearchDetails)) {
            return false;
        }
        CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) obj;
        if (getResourcePermissions().isUserRoleAdmin()
                || getResourcePermissions().isUserRoleOnc()) {
            return isListingActive(listing);
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long acbId = MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY);
            return isAcbValidForCurrentUser(acbId) && isListingActive(listing);
        } else {
            return false;
        }
    }

    private boolean isListingActive(CertifiedProductSearchDetails listing) {
        return CertificationStatusUtil.isActive(listing);
    }

}
