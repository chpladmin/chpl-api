package gov.healthit.chpl.validation.surveillance.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.dao.CertifiedProductDAO;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.dto.CertifiedProductDetailsDTO;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class NewSurveillanceEditionReviewer implements Reviewer {
    private CertifiedProductDAO listingDao;
    private ErrorMessageUtil msgUtil;
    private ResourcePermissionsFactory resourcePermissionsFactory;
    private FF4j ff4j;

    @Autowired
    public NewSurveillanceEditionReviewer(CertifiedProductDAO listingDao, ErrorMessageUtil msgUtil,
            ResourcePermissionsFactory resourcePermissionsFactory, FF4j ff4j) {
        this.listingDao = listingDao;
        this.msgUtil = msgUtil;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
        this.ff4j = ff4j;
    }

    @Override
    public void review(Surveillance surv) {
        if (resourcePermissionsFactory.get().isUserRoleAdmin() || resourcePermissionsFactory.get().isUserRoleOnc()) {
            return;
        }

        String edition = surv.getCertifiedProduct().getEdition();
        if (StringUtils.isEmpty(edition)) {
            edition = determineEdition(surv);
            if (StringUtils.isEmpty(edition)
                    && !ff4j.check(FeatureList.EDITIONLESS)) {
                surv.getErrorMessages().add(msgUtil.getMessage("surveillance.noCreateNoEdition"));
            }
        } else if (!StringUtils.isEmpty(edition)
                && edition.equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear())) {
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
