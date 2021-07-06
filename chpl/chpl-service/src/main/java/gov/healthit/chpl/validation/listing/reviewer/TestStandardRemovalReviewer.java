package gov.healthit.chpl.validation.listing.reviewer;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.CertificationEditionConcept;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Component("testStandardRemovalReviewer")
public class TestStandardRemovalReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public TestStandardRemovalReviewer(ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        if (ff4j.check(FeatureList.OPTIONAL_STANDARDS)) {
            listing.getCertificationResults().stream()
            .filter(cert -> (cert.getTestStandards() != null && cert.getTestStandards().size() > 0))
            .forEach(certResult -> certResult.getTestStandards().stream()
                    .forEach(testStandard -> reviewTestStandard(listing, certResult, testStandard)));
        }
    }

    private void reviewTestStandard(CertifiedProductSearchDetails listing, CertificationResult certResult,
            CertificationResultTestStandard testStandard) {
        String testStandardName = testStandard.getTestStandardName();
        String message = msgUtil.getMessage("listing.criteria.testStandardNotAllowed",
                Util.formatCriteriaNumber(certResult.getCriterion()),
                testStandardName);
        if ((certResult.getOptionalStandards() != null && certResult.getOptionalStandards().size() > 0) || (ff4j.check(FeatureList.OPTIONAL_STANDARDS_ERROR) && isListing2015Edition(listing))) {
            listing.getErrorMessages().add(message);
        } else {
            listing.getWarningMessages().add(message);
        }
    }

    private boolean isListing2015Edition(CertifiedProductSearchDetails listing) {
        return getListingEdition(listing).equals(CertificationEditionConcept.CERTIFICATION_EDITION_2015.getYear());
    }

    private String getListingEdition(CertifiedProductSearchDetails listing) {
        return listing.getCertificationEdition().containsKey(CertifiedProductSearchDetails.EDITION_NAME_KEY)
                ? listing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).toString()
                        : "";
    }
}
