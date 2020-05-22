package gov.healthit.chpl.validation.pendingListing.reviewer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.AccessibilityStandardDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.AdditionalSoftwareDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.AtlDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.IcsSourceDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.QmsStandardDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TargetedUserDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestDataDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestFunctionalityDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestProcedureDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestStandardDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestToolDuplicateReviewer;

@Component("pendingDuplicateDataReviewer")
public class DuplicateDataReviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DuplicateDataReviewer.class);

    private TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer;
    private TestDataDuplicateReviewer testDataDuplicateReviewer;
    private TestToolDuplicateReviewer testToolDuplicateReviewer;
    private TestProcedureDuplicateReviewer testProcedureDuplicateReviewer;
    private TestStandardDuplicateReviewer testStandardDuplicateReviewer;
    private AdditionalSoftwareDuplicateReviewer additionalSoftwareDuplicateReviewer;
    private AccessibilityStandardDuplicateReviewer accessibilityStandardDuplicateReviewer;
    private QmsStandardDuplicateReviewer qmsStandardDuplicateReviewer;
    private IcsSourceDuplicateReviewer icsSourceDuplicateReviewer;
    private AtlDuplicateReviewer atlDuplicateReviewer;
    private TargetedUserDuplicateReviewer targetedUserDuplicateReviewer;

    @Autowired
    public DuplicateDataReviewer(
            @Qualifier("pendingTestFunctionalityDuplicateReviewer") TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer,
            @Qualifier("pendingTestDataDuplicateReviewer") TestDataDuplicateReviewer testDataDuplicateReviewer,
            @Qualifier("pendingTestToolDuplicateReviewer") TestToolDuplicateReviewer testToolDuplicateReviewer,
            @Qualifier("pendingTestProcedureDuplicateReviewer") TestProcedureDuplicateReviewer testProcedureDuplicateReviewer,
            @Qualifier("pendingTestStandardDuplicateReviewer") TestStandardDuplicateReviewer testStandardDuplicateReviewer,
            @Qualifier("pendingAdditionalSoftwareDuplicateReviewer") AdditionalSoftwareDuplicateReviewer additionalSoftwareDuplicateReviewer,
            @Qualifier("pendingAccessibilityStandardDuplicateReviewer") AccessibilityStandardDuplicateReviewer accessibilityStandardDuplicateReviewer,
            @Qualifier("pendingQmsStandardDuplicateReviewer") QmsStandardDuplicateReviewer qmsStandardDuplicateReviewer,
            @Qualifier("pendingIcsSourceDuplicateReviewer") IcsSourceDuplicateReviewer icsSourceDuplicateReviewer,
            @Qualifier("pendingAtlDuplicateReviewer") AtlDuplicateReviewer atlDuplicateReviewer,
            @Qualifier("pendingTargetedUserDuplicateReviewer") TargetedUserDuplicateReviewer targetedUserDuplicateReviewer) {
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
        this.targetedUserDuplicateReviewer = targetedUserDuplicateReviewer;
    }

    @Override
    public void review(final PendingCertifiedProductDTO listing) {
        atlDuplicateReviewer.review(listing);
        accessibilityStandardDuplicateReviewer.review(listing);
        qmsStandardDuplicateReviewer.review(listing);
        icsSourceDuplicateReviewer.review(listing);
        targetedUserDuplicateReviewer.review(listing);

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
