package gov.healthit.chpl.permissions.domains.certificationresults;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("certificationResultsUpdatePermissions")
public class UpdatePermissions extends ActionPermissions {

    @Autowired
    public UpdatePermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

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
