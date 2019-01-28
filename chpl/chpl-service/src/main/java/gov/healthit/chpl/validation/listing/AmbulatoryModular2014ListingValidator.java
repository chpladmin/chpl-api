package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
@Component("ambulatoryModular2014ListingValidator")
public class AmbulatoryModular2014ListingValidator extends Edition2014ListingValidator {

    @Autowired
    @Qualifier("ambulatoryG1G2RequiredData2014Reviewer")
    private AmbulatoryG1G2RequiredData2014Reviewer g1g2Reviewer;

    //TODO: do we want this check here for new-style format 2014 listings?
    //it wasn't in the old validator but i wonder if that was just because of
    //legacy listings.
    @Autowired
    @Qualifier("ambulatoryRequiredTestToolReviewer")
    private AmbulatoryRequiredTestToolReviewer ttReviewer;

    private List<Reviewer> reviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = super.getReviewers();
            reviewers.add(g1g2Reviewer);
            reviewers.add(ttReviewer);
        }
        return reviewers;
    }
}
