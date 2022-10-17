package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import gov.healthit.chpl.validation.listing.reviewer.edition2014.InpatientCompleteRequiredCriteriaReviewer;

@Component("inpatientComplete2014ListingValidator")
public class InpatientComplete2014ListingValidator extends InpatientModular2014ListingValidator {
    @Autowired
    @Qualifier("inpatientCompleteRequiredCriteriaReviewer")
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
