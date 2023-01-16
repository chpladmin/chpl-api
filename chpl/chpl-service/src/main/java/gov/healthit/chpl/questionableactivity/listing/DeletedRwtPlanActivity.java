package gov.healthit.chpl.questionableactivity.listing;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;

@Component
public class DeletedRwtPlanActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        if (!StringUtils.isEmpty(origListing.getRwtPlansUrl()) && StringUtils.isEmpty(newListing.getRwtPlansUrl())) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore("Removed Plans URL: " + origListing.getRwtPlansUrl());
        }
        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REAL_WORLD_TESTING_REMOVED;
    }
}
