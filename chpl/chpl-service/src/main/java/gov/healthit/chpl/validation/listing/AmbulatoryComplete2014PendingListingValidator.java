package gov.healthit.chpl.validation.listing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.AmbulatoryCompleteRequiredCriteriaReviewer;

/**
 * Validate a pending ambulatory complete 2014 listing
 * @author kekey
 *
 */
@Component
public class AmbulatoryComplete2014PendingListingValidator extends AmbulatoryModular2014PendingListingValidator {

    @Autowired AmbulatoryCompleteRequiredCriteriaReviewer criteriaReviewer;
    
    public AmbulatoryComplete2014PendingListingValidator() {
        super();
        getReviewers().add(criteriaReviewer);
    }
}
