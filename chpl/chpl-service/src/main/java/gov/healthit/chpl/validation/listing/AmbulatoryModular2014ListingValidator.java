package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.review.Reviewer;
import gov.healthit.chpl.validation.listing.review.edition2014.AmbulatoryG1G2RequiredData2014Reviewer;
import gov.healthit.chpl.validation.listing.review.edition2014.AmbulatoryRequiredTestToolReviewer;

/**
 * Validation interface for a 2014 Edition Ambulatory Modular EHR listing
 * already uploaded to the CHPL.
 * @author kekey
 *
 */
@Component
public class AmbulatoryModular2014ListingValidator extends Edition2014ListingValidator {

    @Autowired protected AmbulatoryG1G2RequiredData2014Reviewer g1g2Reviewer;
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
