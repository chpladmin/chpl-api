package gov.healthit.chpl.validation.listing.reviewer.edition2015;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.AtlDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestFunctionalityDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestProcedureDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestStandardDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestToolDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate.AccessibilityStandard2015DuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate.AdditionalSoftware2015DuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate.IcsSource2015DuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate.QmsStandard2015DuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate.TargetedUser2015DuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2015.duplicate.TestData2015DuplicateReviewer;

@Component("duplicateData2015Reviewer")
public class DuplicateData2015Reviewer implements Reviewer {
    private TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer;
    private TestData2015DuplicateReviewer testDataDuplicateReviewer;
    private TestToolDuplicateReviewer testToolDuplicateReviewer;
    private TestProcedureDuplicateReviewer testProcedureDuplicateReviewer;
    private TestStandardDuplicateReviewer testStandardDuplicateReviewer;
    private AdditionalSoftware2015DuplicateReviewer additionalSoftwareDuplicateReviewer;
    private AccessibilityStandard2015DuplicateReviewer accessibilityStandardDuplicateReviewer;
    private QmsStandard2015DuplicateReviewer qmsStandardDuplicateReviewer;
    private IcsSource2015DuplicateReviewer icsSourceDuplicateReviewer;
    private AtlDuplicateReviewer atlDuplicateReviewer;
    private TargetedUser2015DuplicateReviewer targetedUser2015DuplicateReviewer;

    @Autowired
    public DuplicateData2015Reviewer(
            @Qualifier("testFunctionalityDuplicateReviewer") TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer,
            @Qualifier("testData2015DuplicateReviewer") TestData2015DuplicateReviewer testDataDuplicateReviewer,
            @Qualifier("testToolDuplicateReviewer") TestToolDuplicateReviewer testToolDuplicateReviewer,
            @Qualifier("testProcedureDuplicateReviewer") TestProcedureDuplicateReviewer testProcedureDuplicateReviewer,
            @Qualifier("testStandardDuplicateReviewer") TestStandardDuplicateReviewer testStandardDuplicateReviewer,
            @Qualifier("additionalSoftware2015DuplicateReviewer") AdditionalSoftware2015DuplicateReviewer additionalSoftwareDuplicateReviewer,
            @Qualifier("accessibilityStandard2015DuplicateReviewer") AccessibilityStandard2015DuplicateReviewer accessibilityStandardDuplicateReviewer,
            @Qualifier("qmsStandard2015DuplicateReviewer") QmsStandard2015DuplicateReviewer qmsStandardDuplicateReviewer,
            @Qualifier("icsSource2015DuplicateReviewer") IcsSource2015DuplicateReviewer icsSourceDuplicateReviewer,
            @Qualifier("atlDuplicateReviewer") AtlDuplicateReviewer atlDuplicateReviewer,
            @Qualifier("targetedUser2015DuplicateReviewer") TargetedUser2015DuplicateReviewer targetedUser2015DuplicateReviewer) {
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
    public void review(CertifiedProductSearchDetails listing) {
        atlDuplicateReviewer.review(listing);
        accessibilityStandardDuplicateReviewer.review(listing);
        qmsStandardDuplicateReviewer.review(listing);
        icsSourceDuplicateReviewer.review(listing);
        targetedUser2015DuplicateReviewer.review(listing);

        for (CertificationResult cr : listing.getCertificationResults()) {
            additionalSoftwareDuplicateReviewer.review(listing, cr);
            testToolDuplicateReviewer.review(listing, cr);
            testProcedureDuplicateReviewer.review(listing, cr);
            testDataDuplicateReviewer.review(listing, cr);
            testStandardDuplicateReviewer.review(listing, cr);
            testFunctionalityDuplicateReviewer.review(listing, cr);
        }
    }

}
