package gov.healthit.chpl.questionableactivity.listing;

import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;

@Component
public class UpdateCurrentCertificationStatusActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        CertificationStatusEvent prev = origListing.getCurrentStatus();
        CertificationStatusEvent curr = newListing.getCurrentStatus();
        if (!prev.getStatus().getId().equals(curr.getStatus().getId())) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(prev.getStatus().getName());
            activity.setAfter(curr.getStatus().getName());
            activity.setCertificationStatusChangeReason(curr.getReason());
        }

        return Arrays.asList(activity);
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CERTIFICATION_STATUS_EDITED_CURRENT;
    }
}
