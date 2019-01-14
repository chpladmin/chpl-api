package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;
import gov.healthit.chpl.validation.pendingListing.reviewer.edition2014.InpatientCompleteRequiredCriteriaReviewer;

@Component("inpatientComplete2014PendingListingValidator")
public class InpatientComplete2014PendingListingValidator extends InpatientModular2014PendingListingValidator {
    @Autowired
    @Qualifier("pendingInpatientCompleteRequiredCriteriaReviewer")
    private InpatientCompleteRequiredCriteriaReviewer reqCriteriaReviewer;

    private List<Reviewer> reviewers;

    @Override
    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = super.getReviewers();
            reviewers.add(reqCriteriaReviewer);
        }
        return reviewers;
    }
}
