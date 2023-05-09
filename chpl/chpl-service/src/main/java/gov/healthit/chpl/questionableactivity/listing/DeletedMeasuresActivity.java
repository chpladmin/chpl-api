package gov.healthit.chpl.questionableactivity.listing;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.ListingMeasure;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;

@Component
public class DeletedMeasuresActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        List<QuestionableActivityListing> measuresRemovedActivities = new ArrayList<QuestionableActivityListing>();
        if (origListing.getMeasures() != null && origListing.getMeasures().size() > 0
                && newListing.getMeasures() != null && newListing.getMeasures().size() > 0) {
            for (ListingMeasure origMeasure : origListing.getMeasures()) {
                Optional<ListingMeasure> matchingNewMeasure = newListing.getMeasures().stream()
                    .filter(newMeasure -> origMeasure.getId().equals(newMeasure.getId()))
                    .findAny();
                if (!matchingNewMeasure.isPresent()) {
                    QuestionableActivityListing activity = new QuestionableActivityListing();
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
                    QuestionableActivityListing activity = new QuestionableActivityListing();
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
