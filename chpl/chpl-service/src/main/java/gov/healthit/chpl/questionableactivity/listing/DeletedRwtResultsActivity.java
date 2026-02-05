package gov.healthit.chpl.questionableactivity.listing;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;

@Component
public class DeletedRwtResultsActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListing activity = null;
        if (!ObjectUtils.isEmpty(origListing.getRwtResultsUrl()) && ObjectUtils.isEmpty(newListing.getRwtResultsUrl())) {
            activity = new QuestionableActivityListing();
            activity.setBefore("Removed Results URL: " + origListing.getRwtResultsUrl());
        }
        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REAL_WORLD_TESTING_REMOVED;
    }
}
