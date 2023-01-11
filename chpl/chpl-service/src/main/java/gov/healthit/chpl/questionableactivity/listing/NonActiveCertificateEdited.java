package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;

import org.springframework.stereotype.Component;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;

@Component
public class NonActiveCertificateEdited implements ListingActivity {

    @Override
    public List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing) {
        if (!newListing.isCertificateActive()) {
            return List.of(QuestionableActivityListingDTO.builder().build());
        } else {
            return null;
        }
    }

    @Override
    public QuestionableActivityTriggerConcept getTriggerType() {
        return QuestionableActivityTriggerConcept.NON_ACTIVE_CERTIFIFCATE_EDITED;
    }

}
