package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListing;

@Component
public class NonActiveCertificateEdited implements ListingActivity {

    @Override
    public List<QuestionableActivityListing> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        if (!origListing.isCertificateActive()) {
            return List.of(QuestionableActivityListing.builder().build());
        } else {
            return null;
        }
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.NON_ACTIVE_CERTIFIFCATE_EDITED;
    }

}
