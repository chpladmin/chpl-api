package gov.healthit.chpl.permissions.domains.certifiedproduct;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certifiedProductUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Autowired
    private FF4j ff4j;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
                return false; // Listing is required so certification edition may be checked
            }
           Long acbId = (Long) obj;
            return isAcbValidForCurrentUser(acbId);
        } else {
            return false;
        }
    }

    @Override
    public boolean hasAccess(final Object obj, final Object listingObj) {
        if (!(obj instanceof Long) || !(listingObj instanceof CertifiedProductSearchDetails)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long acbId = (Long) obj;
            if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
                CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) listingObj;
                return !listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY)
                        .toString().equalsIgnoreCase("2014") && isAcbValidForCurrentUser(acbId);
            } else {
                return isAcbValidForCurrentUser(acbId);
            }
        } else {
            return false;
        }
    }
}
