package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.AmbulatoryG1G2RequiredData2014Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.AmbulatoryRequiredTestToolReviewer;

/**
 * Validation interface for a 2014 Edition Ambulatory Modular EHR listing
 * in pending state on the CHPL.
 * @author kekey
 *
 */
@Component("ambulatoryModular2014PendingListingValidator")
public class AmbulatoryModular2014PendingListingValidator extends Edition2014PendingListingValidator {

    @Autowired
    @Qualifier("pendingAmbulatoryG1G2RequiredData2014Reviewer")
    private AmbulatoryG1G2RequiredData2014Reviewer g1g2Reviewer;

    @Autowired
    @Qualifier("pendingAmbulatoryRequiredTestToolReviewer")
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
