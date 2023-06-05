package gov.healthit.chpl.questionableactivity.listing;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;

@Component
public class Updated2014EditionListingActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListing activity = null;
        if (origListing.getCertificationEdition().get(CertifiedProductSearchDetails.EDITION_NAME_KEY).equals("2014")) {
            activity = new QuestionableActivityListing();
            activity.setBefore(null);
            activity.setAfter(null);
        }

        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.EDITION_2014_EDITED;
    }
}
