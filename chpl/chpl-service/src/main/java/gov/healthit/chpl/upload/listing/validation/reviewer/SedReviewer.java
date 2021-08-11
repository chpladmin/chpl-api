package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

@Component("listingUploadSedReviewer")
public class SedReviewer {

    private UcdProcessReviewer ucdProcessReviewer;
    private TestTaskReviewer testTaskReviewer;
    private TestParticipantReviewer testParticipantReviewer;

    @Autowired
    public SedReviewer(@Qualifier("listingUploadUcdProcessReviewer") UcdProcessReviewer ucdProcessReviewer,
            @Qualifier("listingUploadTestTaskReviewer") TestTaskReviewer testTaskReviewer,
            @Qualifier("listingUploadTestParticipantReviewer") TestParticipantReviewer testParticipantReviewer) {
        this.ucdProcessReviewer = ucdProcessReviewer;
        this.testTaskReviewer = testTaskReviewer;
        this.testParticipantReviewer = testParticipantReviewer;
    }

    public void review(CertifiedProductSearchDetails listing) {
        ucdProcessReviewer.review(listing);
        testTaskReviewer.review(listing);
        testParticipantReviewer.review(listing);
    }
}
