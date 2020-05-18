package gov.healthit.chpl.validation.listing.reviewer.edition2014;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationResult;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.AtlDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestFunctionalityDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestStandardDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.duplicate.TestToolDuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate.AdditionalSoftware2014DuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate.QmsStandard2014DuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate.TestData2014DuplicateReviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.duplicate.TestProcedure2014DuplicateReviewer;

@Component("duplicateData2014Reviewer")
public class DuplicateData2014Reviewer implements Reviewer {
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
            @Qualifier("qmsStandard2014DuplicateReviewer") QmsStandard2014DuplicateReviewer qmsStandard2014DuplicateReviewer,
            @Qualifier("testFunctionalityDuplicateReviewer") TestFunctionalityDuplicateReviewer testFunctionalityDuplicateReviewer,
            @Qualifier("additionalSoftware2014DuplicateReviewer") AdditionalSoftware2014DuplicateReviewer additionalSoftwareDuplicateReviewer,
            @Qualifier("testToolDuplicateReviewer") TestToolDuplicateReviewer testToolDuplicateReviewer,
            @Qualifier("testStandardDuplicateReviewer") TestStandardDuplicateReviewer testStandardDuplicateReviewer,
            @Qualifier("testProcedure2014DuplicateReviewer") TestProcedure2014DuplicateReviewer testProcedureDuplicateReviewer,
            @Qualifier("testData2014DuplicateReviewer") TestData2014DuplicateReviewer testDataDuplicateReviewer,
            @Qualifier("atlDuplicateReviewer") AtlDuplicateReviewer atlDuplicateReviewer) {
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
    public void review(CertifiedProductSearchDetails listing) {
        atlDuplicateReviewer.review(listing);
        qmsStandardDuplicateReviewer.review(listing);

        for (CertificationResult cr : listing.getCertificationResults()) {
            testFunctionalityDuplicateReviewer.review(listing, cr);
            additionalSoftwareDuplicateReviewer.review(listing, cr);
            testToolDuplicateReviewer.review(listing, cr);
            testStandardDuplicateReviewer.review(listing, cr);
            testProcedureDuplicateReviewer.review(listing, cr);
            testDataDuplicateReviewer.review(listing, cr);
        }
    }
}
