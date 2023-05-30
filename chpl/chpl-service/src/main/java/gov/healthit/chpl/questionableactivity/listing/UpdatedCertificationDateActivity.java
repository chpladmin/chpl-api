package gov.healthit.chpl.questionableactivity.listing;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import com.nimbusds.oauth2.sdk.util.StringUtils;

import gov.healthit.chpl.domain.CertificationStatusEvent;
import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityListing;
import gov.healthit.chpl.util.DateUtil;

@Component
public class UpdatedCertificationDateActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListing activity = null;
        LocalDate origCertificationDate = DateUtil.toLocalDate(origListing.getCertificationDate());
        LocalDate updatedCertificationDate = DateUtil.toLocalDate(newListing.getCertificationDate());
        CertificationStatusEvent newListingActiveStatusEvent = newListing.getOldestStatus();

        if (origCertificationDate == null && updatedCertificationDate != null) {
            activity = new QuestionableActivityListing();
            activity.setBefore(null);
            activity.setAfter(updatedCertificationDate.toString());
            if (newListingActiveStatusEvent != null && !StringUtils.isBlank(newListingActiveStatusEvent.getReason())) {
                activity.setCertificationStatusChangeReason(newListingActiveStatusEvent.getReason());
            }
        } else if (origCertificationDate != null && updatedCertificationDate == null) {
            activity = new QuestionableActivityListing();
            activity.setBefore(origCertificationDate.toString());
            activity.setAfter(null);
            if (newListingActiveStatusEvent != null && !StringUtils.isBlank(newListingActiveStatusEvent.getReason())) {
                activity.setCertificationStatusChangeReason(newListingActiveStatusEvent.getReason());
            }
        } else if (!origCertificationDate.isEqual(updatedCertificationDate)) {
            activity = new QuestionableActivityListing();
            activity.setBefore(origCertificationDate.toString());
            activity.setAfter(updatedCertificationDate.toString());
            if (newListingActiveStatusEvent != null && !StringUtils.isBlank(newListingActiveStatusEvent.getReason())) {
                activity.setCertificationStatusChangeReason(newListingActiveStatusEvent.getReason());
            }
        }

        if (activity != null) {
            return Stream.of(activity).toList();
        }
        return null;
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.CERTIFICATION_DATE_EDITED;
    }

}
