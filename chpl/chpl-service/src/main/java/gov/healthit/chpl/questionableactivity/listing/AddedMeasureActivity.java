package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

public class AddedMeasureActivity extends ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListingDTO> measuresAddedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getMeasures() != null && origListing.getMeasures().size() > 0
                && newListing.getMeasures() != null && newListing.getMeasures().size() > 0) {
            for (ListingMeasure newMeasure : newListing.getMeasures()) {
                Optional<ListingMeasure> matchingOrigMeasure = origListing.getMeasures().stream()
                    .filter(origMeasure -> origMeasure.getId().equals(newMeasure.getId()))
                    .findAny();
                if (!matchingOrigMeasure.isPresent()) {
                    QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                    activity.setBefore(null);
                    activity.setAfter(newMeasure.getMeasureType().getName()
                            + " measure " + newMeasure.getMeasure().getName()
                            + " for " + newMeasure.getMeasure().getAbbreviation());
                    measuresAddedActivities.add(activity);
                }
            }
        } else if (newListing.getMeasures() != null
                && (origListing.getMeasures() == null || origListing.getMeasures().size() == 0)) {
            for (ListingMeasure newMeasure : newListing.getMeasures()) {
                    QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                    activity.setBefore(null);
                    activity.setAfter(newMeasure.getMeasureType().getName()
                            + " measure " + newMeasure.getMeasure().getName()
                            + " for " + newMeasure.getMeasure().getAbbreviation());
                    measuresAddedActivities.add(activity);
            }
        }
        return measuresAddedActivities;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.MEASURE_ADDED;
    }
}
