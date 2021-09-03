package gov.healthit.chpl.upload.listing.validation.reviewer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("listingUploadSedReviewer")
public class SedReviewer {

    private UcdProcessReviewer ucdProcessReviewer;
    private TestTaskReviewer testTaskReviewer;
    private TestParticipantReviewer testParticipantReviewer;
    private ErrorMessageUtil msgUtil;

    @Autowired
    public SedReviewer(@Qualifier("listingUploadUcdProcessReviewer") UcdProcessReviewer ucdProcessReviewer,
            @Qualifier("listingUploadTestTaskReviewer") TestTaskReviewer testTaskReviewer,
            @Qualifier("listingUploadTestParticipantReviewer") TestParticipantReviewer testParticipantReviewer,
            ErrorMessageUtil msgUtil) {
        this.ucdProcessReviewer = ucdProcessReviewer;
        this.testTaskReviewer = testTaskReviewer;
        this.testParticipantReviewer = testParticipantReviewer;
        this.msgUtil = msgUtil;
    }

    public void review(CertifiedProductSearchDetails listing) {
        reviewUnusedTasksAndParticipants(listing);
        ucdProcessReviewer.review(listing);
        testTaskReviewer.review(listing);
        testParticipantReviewer.review(listing);
    }

    private void reviewUnusedTasksAndParticipants(CertifiedProductSearchDetails listing) {
        listing.getSed().getUnusedTestTaskUniqueIds().stream()
            .forEach(unusedTestTask -> listing.getWarningMessages().add(msgUtil.getMessage("listing.sed.unusedTestTask", unusedTestTask)));
        listing.getSed().getUnusedTestParticipantUniqueIds().stream()
        .forEach(unusedTestParticipant -> listing.getWarningMessages().add(msgUtil.getMessage("listing.sed.unusedTestParticipant", unusedTestParticipant)));
    }
}
