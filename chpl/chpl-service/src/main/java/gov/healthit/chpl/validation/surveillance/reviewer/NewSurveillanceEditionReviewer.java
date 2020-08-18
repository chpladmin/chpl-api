package gov.healthit.chpl.validation.surveillance.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissions;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class NewSurveillanceEditionReviewer implements Reviewer {
    private CertifiedProductDAO listingDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissions resourcePermissions;

    @Autowired
    public NewSurveillanceEditionReviewer(CertifiedProductDAO listingDao, ErrorMessageUtil msgUtil,
            ResourcePermissions resourcePermissions) {
        this.listingDao = listingDao;
        this.msgUtil = msgUtil;
        this.resourcePermissions = resourcePermissions;
    }

    @Override
    public void review(Surveillance surv) {
        if (resourcePermissions.isUserRoleAdmin() || resourcePermissions.isUserRoleOnc()) {
            return;
        }

        String edition = surv.getCertifiedProduct().getEdition();
        if (StringUtils.isEmpty(edition)) {
            edition = determineEdition(surv);
            if (StringUtils.isEmpty(edition)) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.noCreateNoEdition"));
            }
        } else if (edition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.noCreate2014"));
        }
    }

    private String determineEdition(Surveillance surv) {
        String edition = null;
        if (surv.getCertifiedProduct() != null && surv.getCertifiedProduct().getId() != null) {
            try {
                CertifiedProductDetailsDTO listing = listingDao.getDetailsById(surv.getCertifiedProduct().getId());
                if (listing != null) {
                    edition = listing.getYear();
                }
            } catch (EntityRetrievalException ignore) { }
        }
        return edition;
    }
}
