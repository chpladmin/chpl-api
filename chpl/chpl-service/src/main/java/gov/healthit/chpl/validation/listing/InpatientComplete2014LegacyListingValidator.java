package gov.healthit.chpl.validation.listing;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.edition2014.InpatientCompleteRequiredCriteriaReviewer;

/**
 * Reviews legacy (CHP-) listings for 2014 Inpatient, Complete EHR listings
 * @author kekey
 *
 */
@Component
public class InpatientComplete2014LegacyListingValidator extends InpatientModular2014LegacyListingValidator {
    @Autowired InpatientCompleteRequiredCriteriaReviewer reqCriteriaReviewer;
    
    public InpatientComplete2014LegacyListingValidator() {
        super();
        getReviewers().add(reqCriteriaReviewer);
    }
}
