package gov.healthit.chpl.questionableactivity.listing;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

@Component
public class DeletedSurveillanceActivity implements ListingActivity{

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        if (origListing.getSurveillance() != null && origListing.getSurveillance().size() > 0
                && (newListing.getSurveillance() == null
                || newListing.getSurveillance().size() < origListing.getSurveillance().size())) {

            activity = new QuestionableActivityListingDTO();
            activity.setBefore(null);
            activity.setAfter(null);
        }
        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.SURVEILLANCE_REMOVED;
    }
}
