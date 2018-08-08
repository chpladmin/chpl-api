package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.AmbulatoryG1G2RequiredData2014Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.AmbulatoryRequiredTestToolReviewer;

/**
 * Validation interface for a 2014 Edition Ambulatory Modular EHR listing
 * already uploaded to the CHPL.
 * @author kekey
 *
 */
@Component
public class AmbulatoryModular2014ListingValidator extends Edition2014ListingValidator {

    @Autowired protected AmbulatoryG1G2RequiredData2014Reviewer g1g2Reviewer;
    
    //TODO: do we want this check here for new-style format 2014 listings?
    //it wasn't in the old validator but i wonder if that was just because of 
    //legacy listings.
    @Autowired protected AmbulatoryRequiredTestToolReviewer ttReviewer;
    
    public AmbulatoryModular2014ListingValidator() {
        super();
        reviewers.add(g1g2Reviewer);
        reviewers.add(ttReviewer);
    }
    
    @Override
    public List<Reviewer> getReviewers() {
        return reviewers;
    }
}
