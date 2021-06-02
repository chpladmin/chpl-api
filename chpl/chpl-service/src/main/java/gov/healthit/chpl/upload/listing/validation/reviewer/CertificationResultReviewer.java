package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component
public class CertificationResultReviewer {
    private TestToolReviewer testToolReviewer;
    private TestDataReviewer testDataReviewer;
    private TestProcedureReviewer testProcedureReviewer;
    private TestFunctionalityReviewer testFunctionalityReviewer;
    private TestStandardReviewer testStandardReviewer;
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;
    private ErrorMessageUtil msgUtil;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public CertificationResultReviewer(@Qualifier("listingUploadTestToolReviewer") TestToolReviewer testToolReviewer,
            @Qualifier("listingUploadTestDataReviewer") TestDataReviewer testDataReviewer,
            @Qualifier("listingUploadTestProcedureReviewer") TestProcedureReviewer testProcedureReviewer,
            @Qualifier("listingUploadTestFunctionalityReviewer") TestFunctionalityReviewer testFunctionalityReviewer,
            @Qualifier("listingUploadTestStandardReviewer") TestStandardReviewer testStandardReviewer,
            @Qualifier("uploadedListingUnattestedCriteriaWithDataReviewer") UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer,
            ErrorMessageUtil msgUtil) {
        this.testToolReviewer = testToolReviewer;
        this.testDataReviewer = testDataReviewer;
        this.testProcedureReviewer = testProcedureReviewer;
        this.testFunctionalityReviewer = testFunctionalityReviewer;
        this.testStandardReviewer = testStandardReviewer;
        this.unattestedCriteriaWithDataReviewer = unattestedCriteriaWithDataReviewer;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        if (listing.getCertificationResults() == null) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationResults"));
            return;
        } else if (listing.getCertificationResults().size() == 0 || hasNoAttestedCriteria(listing)) {
            listing.getErrorMessages().add(msgUtil.getMessage("listing.missingCertificationResults"));
        }
        testToolReviewer.review(listing);
        testDataReviewer.review(listing);
        testProcedureReviewer.review(listing);
        testFunctionalityReviewer.review(listing);
        testStandardReviewer.review(listing);
        unattestedCriteriaWithDataReviewer.review(listing);
    }

    private boolean hasNoAttestedCriteria(CertifiedProductSearchDetails listing) {
        return listing.getCertificationResults().stream()
                .filter(certResult -> certResult.isSuccess())
                .count() == 0;
    }
}
