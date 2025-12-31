package gov.healthit.chpl.permissions.domains.surveillance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.changerequest.dao.DeveloperCertificationBodyMapDAO;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component("surveillanceDeleteActionPermissions")
public class DeleteActionPermissions extends ActionPermissions {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public DeleteActionPermissions(ResourcePermissionsFactory resourcePermissionsFactory,
            CertifiedProductDAO certifiedProductDao,
            DeveloperCertificationBodyMapDAO developerCertificationBodyMapDao,
            ErrorMessageUtil msgUtil) {
        super(resourcePermissionsFactory, certifiedProductDao, developerCertificationBodyMapDao);
        this.msgUtil = msgUtil;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Long)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Long listingId = (Long) obj;
            CertifiedProductDTO listing = null;
            try {
                if (listingId != null) {
                    listing = getCertifiedProductDao().getById(listingId);
                }
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Could not find listing with ID " + listingId);
            }

            if (isListing2014Edition(listing)) {
                throw new AccessDeniedException(msgUtil.getMessage("surveillance.noCreate2014"));
            }
            if (listing != null && listing.getCertificationBodyId() != null
                    && isAcbValidForCurrentUser(listing.getCertificationBodyId())) {
                return true;
            }
            return false;
        } else {
            return false;
        }
    }

    private boolean isListing2014Edition(CertifiedProductDTO listing) {
        return listing != null
                && listing.getCertificationEditionId() != null
                && listing.getCertificationEditionId().equals(
                        CertificationEditionConcept.CERTIFICATION_EDITION_2014.getId());
    }
}
