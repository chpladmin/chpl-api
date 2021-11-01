package gov.healthit.chpl.questionableactivity.listing;

import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

@Component
public class UpdatedCertificationStatusDate implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        CertificationStatusEvent prev = origListing.getCurrentStatus();
        CertificationStatusEvent curr = newListing.getCurrentStatus();
        Calendar displayDate = Calendar.getInstance(TimeZone.getTimeZone(ZoneOffset.UTC));
        if (prev.getEventDate().longValue() != curr.getEventDate().longValue()) {
            activity = new QuestionableActivityListingDTO();
            displayDate.setTimeInMillis(prev.getEventDate());
            activity.setBefore(displayDate.getTime().toString());
            displayDate.setTimeInMillis(curr.getEventDate());
            activity.setAfter(displayDate.getTime().toString());
            activity.setCertificationStatusChangeReason(curr.getReason());
        }

        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        //TODO: make sure this is correct
        return QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_DATE_EDITED_CURRENT;
    }

}
