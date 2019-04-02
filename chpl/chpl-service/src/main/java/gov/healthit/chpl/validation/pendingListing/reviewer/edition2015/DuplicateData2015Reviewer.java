package gov.healthit.chpl.validation.pendingListing.reviewer.edition2015;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.AtlDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestFunctionalityDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestStandardDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestToolDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.AccessibilityStandard2015DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.AdditionalSoftware2015DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.IcsSource2015DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.QmsStandard2015DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TargetedUser2015DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestData2015DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2015.duplicate.TestProcedure2015DuplicateReviewer;

@Component("pendingDuplicateData2015Reviewer")
public class DuplicateData2015Reviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DuplicateData2015Reviewer.class);

    private TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer;
    private TestData2015DuplicateReviewer testDataDuplicateReviewer;
    private TestToolDuplicateReviewer testToolDuplicateReviewer;
    private TestProcedure2015DuplicateReviewer testProcedureDuplicateReviewer;
    private TestStandardDuplicateReviewer testStandardDuplicateReviewer;
    private AdditionalSoftware2015DuplicateReviewer additionalSoftwareDuplicateReviewer;
    private AccessibilityStandard2015DuplicateReviewer accessibilityStandardDuplicateReviewer;
    private QmsStandard2015DuplicateReviewer qmsStandardDuplicateReviewer;
    private IcsSource2015DuplicateReviewer icsSourceDuplicateReviewer;
    private AtlDuplicateReviewer atlDuplicateReviewer;
    private TargetedUser2015DuplicateReviewer targetedUser2015DuplicateReviewer;

    @Autowired
    public DuplicateData2015Reviewer(TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer,
            TestData2015DuplicateReviewer testDataDuplicateReviewer,
            TestToolDuplicateReviewer testToolDuplicateReviewer,
            TestProcedure2015DuplicateReviewer testProcedureDuplicateReviewer,
            TestStandardDuplicateReviewer testStandardDuplicateReviewer,
            AdditionalSoftware2015DuplicateReviewer additionalSoftwareDuplicateReviewer,
            AccessibilityStandard2015DuplicateReviewer accessibilityStandardDuplicateReviewer,
            QmsStandard2015DuplicateReviewer qmsStandardDuplicateReviewer,
            IcsSource2015DuplicateReviewer icsSourceDuplicateReviewer,
            AtlDuplicateReviewer atlDuplicateReviewer,
            TargetedUser2015DuplicateReviewer targetedUser2015DuplicateReviewer) {
        this.testFunctionalityDuplicateReviewer = testFunctionalityDuplicateReviewer;
        this.testDataDuplicateReviewer = testDataDuplicateReviewer;
        this.testToolDuplicateReviewer = testToolDuplicateReviewer;
        this.testProcedureDuplicateReviewer = testProcedureDuplicateReviewer;
        this.testStandardDuplicateReviewer = testStandardDuplicateReviewer;
        this.additionalSoftwareDuplicateReviewer = additionalSoftwareDuplicateReviewer;
        this.accessibilityStandardDuplicateReviewer = accessibilityStandardDuplicateReviewer;
        this.qmsStandardDuplicateReviewer = qmsStandardDuplicateReviewer;
        this.icsSourceDuplicateReviewer = icsSourceDuplicateReviewer;
        this.atlDuplicateReviewer = atlDuplicateReviewer;
        this.targetedUser2015DuplicateReviewer = targetedUser2015DuplicateReviewer;
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        atlDuplicateReviewer.review(listing);
        accessibilityStandardDuplicateReviewer.review(listing);
        qmsStandardDuplicateReviewer.review(listing);
        icsSourceDuplicateReviewer.review(listing);
        targetedUser2015DuplicateReviewer.review(listing);

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
