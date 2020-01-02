package gov.healthit.chpl.permissions.domains.surveillance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;

@Component("surveillanceCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(CreateActionPermissions.class);

    private CertifiedProductDAO cpDao;

    @Autowired
    public CreateActionPermissions(CertifiedProductDAO cpDao) {
        this.cpDao = cpDao;
    }

    @Override
    public boolean hasAccess() {
        return false;
    }

    @Override
    public boolean hasAccess(Object obj) {
        if (!(obj instanceof Surveillance)) {
            return false;
        } else if (getResourcePermissions().isUserRoleAdmin() || getResourcePermissions().isUserRoleOnc()) {
            return true;
        } else if (getResourcePermissions().isUserRoleAcbAdmin()) {
            Surveillance surv = (Surveillance) obj;
            CertifiedProductDTO listing = null;
            try {
                if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
                    listing = cpDao.getById(surv.getCertifiedProduct().getId());
                }
            } catch (EntityRetrievalException ex) {
                LOGGER.error("Could not find listing with ID " + surv.getCertifiedProduct().getId());
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

}
