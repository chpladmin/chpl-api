package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.AccessibilityStandardDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.AdditionalSoftwareDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestDataDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestFunctionalityDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestProcedureDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestStandardDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestToolDuplicateReviewer;

@Component("pendingDuplicateData2015Reviewer")
public class DuplicateData2015Reviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DuplicateData2015Reviewer.class);

    //private ErrorMessageUtil errorMessageUtil;
    private TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer;
    private TestDataDuplicateReviewer testDataDuplicateReviewer;
    private TestToolDuplicateReviewer testToolDuplicateReviewer;
    private TestProcedureDuplicateReviewer testProcedureDuplicateReviewer;
    private TestStandardDuplicateReviewer testStandardDuplicateReviewer;
    private AdditionalSoftwareDuplicateReviewer additionalSoftwareDuplicateReviewer;
    private AccessibilityStandardDuplicateReviewer accessibilityStandardDuplicateReviewer;

    @Autowired
    public DuplicateData2015Reviewer(TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer,
            TestDataDuplicateReviewer testDataDuplicateReviewer,
            TestToolDuplicateReviewer testToolDuplicateReviewer,
            TestProcedureDuplicateReviewer testProcedureDuplicateReviewer,
            TestStandardDuplicateReviewer testStandardDuplicateReviewer,
            AdditionalSoftwareDuplicateReviewer additionalSoftwareDuplicateReviewer,
            AccessibilityStandardDuplicateReviewer accessibilityStandardDuplicateReviewer) {
        this.testFunctionalityDuplicateReviewer = testFunctionalityDuplicateReviewer;
        this.testDataDuplicateReviewer = testDataDuplicateReviewer;
        this.testToolDuplicateReviewer = testToolDuplicateReviewer;
        this.testProcedureDuplicateReviewer = testProcedureDuplicateReviewer;
        this.testStandardDuplicateReviewer = testStandardDuplicateReviewer;
        this.additionalSoftwareDuplicateReviewer = additionalSoftwareDuplicateReviewer;
        this.accessibilityStandardDuplicateReviewer = accessibilityStandardDuplicateReviewer;
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        accessibilityStandardDuplicateReviewer.review(listing);

        for (PendingCertificationResultDTO pcr : listing.getCertificationCriterion()) {
            additionalSoftwareDuplicateReviewer.review(listing, pcr);
            testToolDuplicateReviewer.review(listing, pcr);
            testProcedureDuplicateReviewer.review(listing, pcr);
            testDataDuplicateReviewer.review(listing, pcr);
            testStandardDuplicateReviewer.review(listing, pcr);
            testFunctionalityDuplicateReviewer.review(listing, pcr);
        }
    }

}
