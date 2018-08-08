package gov.healthit.chpl.validation.listing;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.edition2014.AmbulatoryCompleteRequiredCriteriaReviewer;

/**
 * Validation interface for any listing that is already uploaded and confirmed on the CHPL.
 * @author kekey
 *
 */
@Component
public class AmbulatoryComplete2014ListingValidator extends AmbulatoryModular2014ListingValidator {

    @Autowired AmbulatoryCompleteRequiredCriteriaReviewer criteriaReviewer;
    
    public AmbulatoryComplete2014ListingValidator() {
        super();
        getReviewers().add(criteriaReviewer);
    }
}
