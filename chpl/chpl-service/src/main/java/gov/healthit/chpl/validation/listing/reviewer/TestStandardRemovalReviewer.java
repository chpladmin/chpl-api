package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.permissions.ResourcePermissionsFactory;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("testStandardRemovalReviewer")
public class TestStandardRemovalReviewer extends PermissionBasedReviewer {
    private ErrorMessageUtil msgUtil;

    @Autowired
    public TestStandardRemovalReviewer(ErrorMessageUtil msgUtil, ResourcePermissionsFactory resourcePermissionsFactory) {
        super(msgUtil, resourcePermissionsFactory);
        this.msgUtil = msgUtil;
        this.resourcePermissionsFactory = resourcePermissionsFactory;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        listing.getCertificationResults().stream()
                .filter(cert -> (cert.getTestStandards() != null && cert.getTestStandards().size() > 0))
                .forEach(certResult -> certResult.getTestStandards().stream()
                        .forEach(testStandard -> reviewTestStandard(listing, certResult, testStandard)));
    }

    private void reviewTestStandard(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestStandard testStandard) {
        String testStandardName = testStandard.getTestStandardName();
        String message = msgUtil.getMessage("listing.criteria.testStandardNotAllowed",
                Util.formatCriteriaNumber(certResult.getCriterion()),
                testStandardName);
        if ((certResult.getOptionalStandards() != null && certResult.getOptionalStandards().size() > 0)
                || listing.getEdition() == null
                || isListing2015Edition(listing)) {
            addBusinessCriterionError(listing, certResult, message);
        } else {
            listing.addWarningMessage(message);
        }
    }

    private boolean isListing2015Edition(CertifiedProductSearchDetails listing) {
        return getListingEdition(listing).equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
    }

    private String getListingEdition(CertifiedProductSearchDetails listing) {
        return listing.getEdition() != null
                ? listing.getEdition().getName()
                : "";
    }
}
