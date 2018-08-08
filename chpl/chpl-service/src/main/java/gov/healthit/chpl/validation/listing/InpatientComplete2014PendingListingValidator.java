package gov.healthit.chpl.validation.listing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.InpatientCompleteRequiredCriteriaReviewer;

@Component
public class InpatientComplete2014PendingListingValidator extends InpatientModular2014PendingListingValidator {
    @Autowired InpatientCompleteRequiredCriteriaReviewer reqCriteriaReviewer;
    
    public InpatientComplete2014PendingListingValidator() {
        super();
        getReviewers().add(reqCriteriaReviewer);
    }
}
