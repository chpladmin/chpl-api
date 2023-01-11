package gov.healthit.chpl.questionableactivity.listing;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.entity.CertificationStatusType;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;

@Component
public class UpdatedCertificationStatusWithdrawnByDeveloperUnderReviewActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        CertificationStatusType withdrawnByDeveloperUnderReview = CertificationStatusType.WithdrawnByDeveloperUnderReview;
        if (!origListing.getCurrentStatus().getStatus().getName().equals(withdrawnByDeveloperUnderReview.getName())
                && newListing.getCurrentStatus().getStatus().getName().equals(withdrawnByDeveloperUnderReview.getName())) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(origListing.getCurrentStatus().getStatus().getName());
            activity.setAfter(newListing.getCurrentStatus().getStatus().getName());
            activity.setCertificationStatusChangeReason(newListing.getCurrentStatus().getReason());
        }

        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT;
    }
}
