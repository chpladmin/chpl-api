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
import gov.healthit.chpl.validation.listing.reviewer.duplicate.MeasureDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.OptionalStandardDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.PromotingInteroperabilityUserCountReviewer;
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
    private OptionalStandardDuplicateReviewer optionalStandardDuplicateReviewer;
    private AdditionalSoftwareDuplicateReviewer additionalSoftwareDuplicateReviewer;
    private AccessibilityStandardDuplicateReviewer accessibilityStandardDuplicateReviewer;
    private QmsStandardDuplicateReviewer qmsStandardDuplicateReviewer;
    private MeasureDuplicateReviewer measureDuplicateReviewer;
    private IcsSourceDuplicateReviewer icsSourceDuplicateReviewer;
    private AtlDuplicateReviewer atlDuplicateReviewer;
    private TargetedUserDuplicateReviewer targetedUserDuplicateReviewer;
    private PromotingInteroperabilityUserCountReviewer piuReviewer;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public DuplicateDataReviewer(
            @Qualifier("testFunctionalityDuplicateReviewer") TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer,
            @Qualifier("testDataDuplicateReviewer") TestDataDuplicateReviewer testDataDuplicateReviewer,
            @Qualifier("testToolDuplicateReviewer") TestToolDuplicateReviewer testToolDuplicateReviewer,
            @Qualifier("testProcedureDuplicateReviewer") TestProcedureDuplicateReviewer testProcedureDuplicateReviewer,
            @Qualifier("testStandardDuplicateReviewer") TestStandardDuplicateReviewer testStandardDuplicateReviewer,
            @Qualifier("optionalStandardDuplicateReviewer") OptionalStandardDuplicateReviewer optionalStandardDuplicateReviewer,
            @Qualifier("additionalSoftwareDuplicateReviewer") AdditionalSoftwareDuplicateReviewer additionalSoftwareDuplicateReviewer,
            @Qualifier("accessibilityStandardDuplicateReviewer") AccessibilityStandardDuplicateReviewer accessibilityStandardDuplicateReviewer,
            @Qualifier("qmsStandardDuplicateReviewer") QmsStandardDuplicateReviewer qmsStandardDuplicateReviewer,
            @Qualifier("measureDuplicateReviewer") MeasureDuplicateReviewer measureDuplicateReviewer,
            @Qualifier("icsSourceDuplicateReviewer") IcsSourceDuplicateReviewer icsSourceDuplicateReviewer,
            @Qualifier("atlDuplicateReviewer") AtlDuplicateReviewer atlDuplicateReviewer,
            @Qualifier("targetedUserDuplicateReviewer") TargetedUserDuplicateReviewer targetedUserDuplicateReviewer,
            @Qualifier("promotingInteroperabilityUserCountDuplicateReviewer") PromotingInteroperabilityUserCountReviewer piuReviewer) {
        this.testFunctionalityDuplicateReviewer = testFunctionalityDuplicateReviewer;
        this.testDataDuplicateReviewer = testDataDuplicateReviewer;
        this.testToolDuplicateReviewer = testToolDuplicateReviewer;
        this.testProcedureDuplicateReviewer = testProcedureDuplicateReviewer;
        this.testStandardDuplicateReviewer = testStandardDuplicateReviewer;
        this.optionalStandardDuplicateReviewer = optionalStandardDuplicateReviewer;
        this.additionalSoftwareDuplicateReviewer = additionalSoftwareDuplicateReviewer;
        this.accessibilityStandardDuplicateReviewer = accessibilityStandardDuplicateReviewer;
        this.qmsStandardDuplicateReviewer = qmsStandardDuplicateReviewer;
        this.measureDuplicateReviewer = measureDuplicateReviewer;
        this.icsSourceDuplicateReviewer = icsSourceDuplicateReviewer;
        this.atlDuplicateReviewer = atlDuplicateReviewer;
        this.targetedUserDuplicateReviewer = targetedUserDuplicateReviewer;
        this.piuReviewer = piuReviewer;
    }

    @Override
    public void review(CertifiedProductSearchDetails listing) {
        atlDuplicateReviewer.review(listing);
        accessibilityStandardDuplicateReviewer.review(listing);
        qmsStandardDuplicateReviewer.review(listing);
        measureDuplicateReviewer.review(listing);
        icsSourceDuplicateReviewer.review(listing);
        targetedUserDuplicateReviewer.review(listing);
        piuReviewer.review(listing);

        for (CertificationResult cr : listing.getCertificationResults()) {
            additionalSoftwareDuplicateReviewer.review(listing, cr);
            testToolDuplicateReviewer.review(listing, cr);
            testProcedureDuplicateReviewer.review(listing, cr);
            testDataDuplicateReviewer.review(listing, cr);
            testStandardDuplicateReviewer.review(listing, cr);
            optionalStandardDuplicateReviewer.review(listing, cr);
            testFunctionalityDuplicateReviewer.review(listing, cr);
        }
    }

}
