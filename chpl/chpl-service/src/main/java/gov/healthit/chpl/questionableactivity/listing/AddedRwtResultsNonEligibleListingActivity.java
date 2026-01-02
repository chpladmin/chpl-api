package gov.healthit.chpl.questionableactivity.listing;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.service.realworldtesting.RealWorldTestingEligiblityServiceFactory;
import lombok.extern.log4j.Log4j2;

@Log4j2
@Component
public class AddedRwtResultsNonEligibleListingActivity implements ListingActivity {
    private RealWorldTestingEligiblityServiceFactory rwtEligServiceFactory;

    @Autowired
    public AddedRwtResultsNonEligibleListingActivity(RealWorldTestingEligiblityServiceFactory rwtEligServiceFactory) {
        this.rwtEligServiceFactory = rwtEligServiceFactory;
    }

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListing activity = null;
        if (ObjectUtils.isEmpty(origListing.getRwtResultsUrl())
                && !ObjectUtils.isEmpty(newListing.getRwtResultsUrl())
                && !isListingRealWorldTestingEligible(newListing.getId())) {
            activity = new QuestionableActivityListing();
            activity.setAfter("Added Results URL " + newListing.getRwtResultsUrl());
        }
        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.REAL_WORLD_TESTING_ADDED;
    }

    private boolean isListingRealWorldTestingEligible(Long listingId) {
        return rwtEligServiceFactory.getInstance().getRwtEligibilityYearForListing(listingId, LOGGER).getEligibilityYear() != null;
    }
}
