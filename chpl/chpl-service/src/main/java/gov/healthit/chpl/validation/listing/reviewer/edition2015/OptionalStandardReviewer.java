package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.ff4j.FF4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.FeatureList;
import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertificationResultTestStandard;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component("optionalStandardReviewer")
public class OptionalStandardReviewer implements Reviewer {
    private ErrorMessageUtil msgUtil;
    private FF4j ff4j;

    @Autowired
    public OptionalStandardReviewer(ErrorMessageUtil msgUtil, FF4j ff4j) {
        this.msgUtil = msgUtil;
        this.ff4j = ff4j;
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
        if ((certResult.getOptionalStandards() != null && certResult.getOptionalStandards().size() > 0) || ff4j.check(FeatureList.OPTIONAL_STANDARDS_ERROR)) {
            listing.getErrorMessages().add(message);
        } else {
            listing.getWarningMessages().add(message);
        }
    }
}
