package gov.healthit.chpl.questionableactivity.listing;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;
import gov.healthit.chpl.util.DateUtil;

@Component
public class UpdatedCertificationDateActivity implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        QuestionableActivityListingDTO activity = null;
        LocalDate origCertificationDate = DateUtil.toLocalDate(origListing.getCertificationDate());
        LocalDate updatedCertificationDate = DateUtil.toLocalDate(newListing.getCertificationDate());

        if (origCertificationDate == null && updatedCertificationDate != null) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(null);
            activity.setAfter(updatedCertificationDate.toString());
        } else if (origCertificationDate != null && updatedCertificationDate == null) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(origCertificationDate.toString());
            activity.setAfter(null);
        } else if (!origCertificationDate.isEqual(updatedCertificationDate)) {
            activity = new QuestionableActivityListingDTO();
            activity.setBefore(origCertificationDate.toString());
            activity.setAfter(updatedCertificationDate.toString());
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
