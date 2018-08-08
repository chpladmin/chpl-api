package gov.healthit.chpl.validation.listing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.edition2014.InpatientCompleteRequiredCriteriaReviewer;

@Component
public class InpatientComplete2014ListingValidator extends InpatientModular2014ListingValidator {
    @Autowired InpatientCompleteRequiredCriteriaReviewer reqCriteriaReviewer;
    
    public InpatientComplete2014ListingValidator() {
        super();
        getReviewers().add(reqCriteriaReviewer);
    }
}
