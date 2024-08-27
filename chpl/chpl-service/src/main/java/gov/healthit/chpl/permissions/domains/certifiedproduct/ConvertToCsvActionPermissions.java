package gov.healthit.chpl.permissions.domains.certifiedproduct;

import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
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
            return isListingNotRetired(listing);
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long acbId = MapUtils.getLong(listing.getCertifyingBody(), CertifiedProductSearchDetails.ACB_ID_KEY);
            return isAcbValidForCurrentUser(acbId) && isListingInEditableStatus(listing);
        } else {
            return false;
        }
    }

    private boolean isListingNotRetired(CertifiedProductSearchDetails listing) {
        return CertificationStatusUtil.isNotRetired(listing);
    }

    private boolean isListingInEditableStatus(CertifiedProductSearchDetails listing) {
        return CertificationStatusUtil.isNotRetired(listing)
                && !listing.getCurrentStatus().getStatus().getName().equals(CertificationStatusType.SuspendedByOnc.getName())
                && !listing.getCurrentStatus().getStatus().getName().equals(CertificationStatusType.TerminatedByOnc.getName());
    }
}
