package gov.healthit.chpl.questionableactivity.listing;

import java.util.List;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.domain.concept.QuestionableActivityTriggerConcept;
import gov.healthit.chpl.dto.questionableActivity.QuestionableActivityListingDTO;

public abstract class ListingActivity {
    public abstract List<QuestionableActivityListingDTO> check(CertifiedProductSearchDetails origListing, CertifiedProductSearchDetails newListing);
    public abstract QuestionableActivityTriggerConcept getTriggerType();
}
