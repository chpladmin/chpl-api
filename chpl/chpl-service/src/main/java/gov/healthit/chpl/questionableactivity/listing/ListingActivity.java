package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.questionableactivity.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.questionableactivity.dto.QuestionableActivityListingDTO;

public interface ListingActivity {
    List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing);
    QuestionableActivityTriggerConcept getTriggerType();
}
