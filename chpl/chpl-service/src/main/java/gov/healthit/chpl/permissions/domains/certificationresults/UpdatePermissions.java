package gov.healthit.chpl.permissions.domains.certificationresults;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("certificationResultsUpdatePermissions")
public class UpdatePermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(UpdatePermissions.class);

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(final Object obj) {
        if (!(obj instanceof CertifiedProductSearchDetails)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            CertifiedProductSearchDetails listing = (CertifiedProductSearchDetails) obj;
            Long acbId = null;
            try {
                acbId = Long.valueOf(listing.getCertifyingBody()
                    .get(CertifiedProductSearchDetails.ACB_ID_KEY).toString());
            } catch (Exception ex) {
                LOGGER.error("Unable to parse the ACB ID from the listing update request.", ex);
                return false;
            }
            return isAcbValidForCurrentUser(acbId);
        } else {
            return false;
        }
    }

}
