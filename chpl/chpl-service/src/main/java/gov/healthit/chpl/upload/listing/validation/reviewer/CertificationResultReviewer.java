package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component
public class CertificationResultReviewer {
    private TestToolReviewer testToolReviewer;
    private TestDataReviewer testDataReviewer;
    private UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public CertificationResultReviewer(CSVHeaderReviewer csvHeaderReviewer,
            @Qualifier("listingUploadTestToolReviewer") TestToolReviewer testToolReviewer,
            @Qualifier("listingUploadTestDataReviewer") TestDataReviewer testDataReviewer,
            @Qualifier("uploadedListingUnattestedCriteriaWithDataReviewer") UnattestedCriteriaWithDataReviewer unattestedCriteriaWithDataReviewer) {
        this.testToolReviewer = testToolReviewer;
        this.testDataReviewer = testDataReviewer;
        this.unattestedCriteriaWithDataReviewer = unattestedCriteriaWithDataReviewer;
    }

    public void review(CertifiedProductSearchDetails listing) {
        testToolReviewer.review(listing);
        testDataReviewer.review(listing);
        unattestedCriteriaWithDataReviewer.review(listing);
    }
}
