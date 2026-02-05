package gov.healthit.chpl.permissions.domains.certifiedproduct;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingUpdateRequest;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("certifiedProductUpdateActionPermissions")
public class UpdateActionPermissions extends ActionPermissions {

    @Autowired
    public UpdateActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
    }

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
            return (listing.getEdition() == null || !listing.getEdition().getName().equalsIgnoreCase("2014"))
                    && isAcbValidForCurrentUser(acbId);
        } else {
            return false;
        }
    }
}
