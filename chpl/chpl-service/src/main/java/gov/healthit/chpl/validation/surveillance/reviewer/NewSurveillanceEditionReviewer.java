package gov.healthit.chpl.validation.surveillance.reviewer;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationEdition;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.domain.surveillance.Surveillance;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class NewSurveillanceEditionReviewer implements WriteReviewer {
    private ErrorMessageUtil msgUtil;
    private ResourcePermissionsFactory resourcePermissionsFactory;

    @Autowired
    public NewSurveillanceEditionReviewer(ErrorMessageUtil msgUtil,
            ResourcePermissionsFactory resourcePermissionsFactory) {
        this.msgUtil = msgUtil;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing, Surveillance surv) {
        if (resourcePermissionsFactory.get().isUserRoleAdmin() || resourcePermissionsFactory.get().isUserRoleOnc()) {
            return;
        }

        CertificationEdition edition = listing.getEdition();
        if (edition != null
                && !StringUtils.isEmpty(edition.getName())
                && edition.getName().equals(CertificationEditionConcept.CERTIFICATION_EDITION_2014.getYear())) {
            surv.getErrorMessages().add(msgUtil.getMessage("surveillance.noCreate2014"));
        }
    }
}
