package gov.healthit.chpl.permissions.domains.surveillance;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.domains.ActionPermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("surveillanceCreateActionPermissions")
public class CreateActionPermissions extends ActionPermissions {
    private static final Logger LOGGER = LogManager.getLogger(CreateActionPermissions.class);

    private CertifiedProductDAO cpDao;
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public CreateActionPermissions(CertifiedProductDAO cpDao, ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.cpDao = cpDao;
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
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
        if (!ff4j.check(FeatureList.EFFECTIVE_RULE_DATE_PLUS_ONE_WEEK)) {
            return false;
        }

        return listing != null
                && listing.getCertificationEditionId().equals(
                CertificationEditionConcept.CERTIFICATION_EDITION_2014.getId());
    }
}
