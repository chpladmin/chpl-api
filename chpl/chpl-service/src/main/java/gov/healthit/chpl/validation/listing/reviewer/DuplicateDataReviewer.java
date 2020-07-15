package gov.healthit.chpl.validation.listing.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.AccessibilityStandardDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.AdditionalSoftwareDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.AtlDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.IcsSourceDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.QmsStandardDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TargetedUserDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestDataDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestFunctionalityDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestProcedureDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestStandardDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestToolDuplicateReviewer;

@Component("duplicateDataReviewer")
public class DuplicateDataReviewer implements Reviewer {
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
            @Qualifier("testFunctionalityDuplicateReviewer") TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer,
            @Qualifier("testDataDuplicateReviewer") TestDataDuplicateReviewer testDataDuplicateReviewer,
            @Qualifier("testToolDuplicateReviewer") TestToolDuplicateReviewer testToolDuplicateReviewer,
            @Qualifier("testProcedureDuplicateReviewer") TestProcedureDuplicateReviewer testProcedureDuplicateReviewer,
            @Qualifier("testStandardDuplicateReviewer") TestStandardDuplicateReviewer testStandardDuplicateReviewer,
            @Qualifier("additionalSoftwareDuplicateReviewer") AdditionalSoftwareDuplicateReviewer additionalSoftwareDuplicateReviewer,
            @Qualifier("accessibilityStandardDuplicateReviewer") AccessibilityStandardDuplicateReviewer accessibilityStandardDuplicateReviewer,
            @Qualifier("qmsStandardDuplicateReviewer") QmsStandardDuplicateReviewer qmsStandardDuplicateReviewer,
            @Qualifier("icsSourceDuplicateReviewer") IcsSourceDuplicateReviewer icsSourceDuplicateReviewer,
            @Qualifier("atlDuplicateReviewer") AtlDuplicateReviewer atlDuplicateReviewer,
            @Qualifier("targetedUserDuplicateReviewer") TargetedUserDuplicateReviewer targetedUserDuplicateReviewer) {
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
    public void review(CertifiedProductSearchDetails listing) {
        atlDuplicateReviewer.review(listing);
        accessibilityStandardDuplicateReviewer.review(listing);
        qmsStandardDuplicateReviewer.review(listing);
        icsSourceDuplicateReviewer.review(listing);
        targetedUserDuplicateReviewer.review(listing);

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
