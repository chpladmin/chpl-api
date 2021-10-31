package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

public class DeletedMeasuresActivity extends ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListingDTO> measuresRemovedActivities = new ArrayList<QuestionableActivityListingDTO>();
        if (origListing.getMeasures() != null && origListing.getMeasures().size() > 0
                && newListing.getMeasures() != null && newListing.getMeasures().size() > 0) {
            for (ListingMeasure origMeasure : origListing.getMeasures()) {
                Optional<ListingMeasure> matchingNewMeasure = newListing.getMeasures().stream()
                    .filter(newMeasure -> origMeasure.getId().equals(newMeasure.getId()))
                    .findAny();
                if (!matchingNewMeasure.isPresent()) {
                    QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                    activity.setBefore(origMeasure.getMeasureType().getName()
                            + " measure " + origMeasure.getMeasure().getName()
                            + " for " + origMeasure.getMeasure().getAbbreviation());
                    activity.setAfter(null);
                    measuresRemovedActivities.add(activity);
                }
            }
        } else if (origListing.getMeasures() != null
                && (newListing.getMeasures() == null || newListing.getMeasures().size() == 0)) {
            for (ListingMeasure origMeasure : origListing.getMeasures()) {
                    QuestionableActivityListingDTO activity = new QuestionableActivityListingDTO();
                    activity.setBefore(origMeasure.getMeasureType().getName()
                            + " measure " + origMeasure.getMeasure().getName()
                            + " for " + origMeasure.getMeasure().getAbbreviation());
                    activity.setAfter(null);
                    measuresRemovedActivities.add(activity);
            }
        }
        return measuresRemovedActivities;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.MEASURE_REMOVED;
    }
}
