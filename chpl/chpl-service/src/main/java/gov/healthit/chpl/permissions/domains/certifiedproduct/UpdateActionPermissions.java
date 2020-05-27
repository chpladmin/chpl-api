package gov.healthit.chpl.permissions.domains.certifiedproduct;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certifiedProductUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(UpdateActionPermissions.class);

    @Autowired
    private FF4j ff4j;

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof ListingUpdateRequest)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {

            ListingUpdateRequest updateRequest = (ListingUpdateRequest) obj;
            CertifiedProductSearchDetails listing = updateRequest.getListing();
            Long acbId = null;
            try {
                acbId = Long.valueOf(updateRequest.getListing().getCertifyingBody()
                        .get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());
            } catch (Exception ex) {
                LOGGER.error("Unable to parse the ACB ID from the listing update request.", ex);
                return false;
            }
            if (ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
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
