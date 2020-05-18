package gov.healthit.chpl.validation.pendingListing.reviewer.edition2014;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.dto.listing.pending.PendingCertificationResultDTO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.AtlDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestFunctionalityDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestStandardDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.duplicate.TestToolDuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.duplicate.AdditionalSoftware2014DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.duplicate.QmsStandard2014DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.duplicate.TestData2014DuplicateReviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.duplicate.TestProcedure2014DuplicateReviewer;

@Component("pendingDuplicateData2014Reviewer")
public class DuplicateData2014Reviewer implements Reviewer {
    private static final Logger LOGGER = LogManager.getLogger(DuplicateData2014Reviewer.class);

    private QmsStandard2014DuplicateReviewer qmsStandardDuplicateReviewer;
    private TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer;
    private AdditionalSoftware2014DuplicateReviewer additionalSoftwareDuplicateReviewer;
    private TestToolDuplicateReviewer testToolDuplicateReviewer;
    private TestStandardDuplicateReviewer testStandardDuplicateReviewer;
    private TestProcedure2014DuplicateReviewer testProcedureDuplicateReviewer;
    private TestData2014DuplicateReviewer testDataDuplicateReviewer;
    private AtlDuplicateReviewer atlDuplicateReviewer;

    @Autowired
    public DuplicateData2014Reviewer(
            @Qualifier("pendingQmsStandard2014DuplicateReviewer") QmsStandard2014DuplicateReviewer qmsStandard2014DuplicateReviewer,
            @Qualifier("pendingTestFunctionalityDuplicateReviewer") TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer,
            @Qualifier("pendingAdditionalSoftware2014DuplicateReviewer") AdditionalSoftware2014DuplicateReviewer additionalSoftwareDuplicateReviewer,
            @Qualifier("pendingTestToolDuplicateReviewer") TestToolDuplicateReviewer testToolDuplicateReviewer,
            @Qualifier("pendingTestStandardDuplicateReviewer") TestStandardDuplicateReviewer testStandardDuplicateReviewer,
            @Qualifier("pendingTestProcedure2014DuplicateReviewer") TestProcedure2014DuplicateReviewer testProcedureDuplicateReviewer,
            @Qualifier("pendingTestData2014DuplicateReviewer") TestData2014DuplicateReviewer testDataDuplicateReviewer,
            @Qualifier("pendingAtlDuplicateReviewer") AtlDuplicateReviewer atlDuplicateReviewer) {
        this.qmsStandardDuplicateReviewer = qmsStandard2014DuplicateReviewer;
        this.testFunctionalityDuplicateReviewer = testFunctionalityDuplicateReviewer;
        this.additionalSoftwareDuplicateReviewer = additionalSoftwareDuplicateReviewer;
        this.testToolDuplicateReviewer = testToolDuplicateReviewer;
        this.testStandardDuplicateReviewer = testStandardDuplicateReviewer;
        this.testProcedureDuplicateReviewer = testProcedureDuplicateReviewer;
        this.testDataDuplicateReviewer = testDataDuplicateReviewer;
        this.atlDuplicateReviewer = atlDuplicateReviewer;
    }

    @Override
    public void review(PendingCertifiedProductDTO listing) {
        atlDuplicateReviewer.review(listing);
        qmsStandardDuplicateReviewer.review(listing);

        for (PendingCertificationResultDTO pcr : listing.getCertificationCriterion()) {
            testFunctionalityDuplicateReviewer.review(listing, pcr);
            additionalSoftwareDuplicateReviewer.review(listing, pcr);
            testToolDuplicateReviewer.review(listing, pcr);
            testStandardDuplicateReviewer.review(listing, pcr);
            testProcedureDuplicateReviewer.review(listing, pcr);
            testDataDuplicateReviewer.review(listing, pcr);
        }
    }
}
