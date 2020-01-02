package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

/**
 * A concrete validation implementation that does not check for any listing errors.
 * @author kekey
 *
 */
@Component
public class AllowedListingValidator extends Validator {
    private DeveloperStatusReviewer devStatusReviewer;

    private List<Reviewer> reviewers;
    private List<ComparisonReviewer> comparisonReviewers;

    @Autowired
    public AllowedListingValidator(@Qualifier("developerStatusReviewer") DeveloperStatusReviewer devStatusReviewer) {
        this.devStatusReviewer = devStatusReviewer;
        comparisonReviewers = new ArrayList<ComparisonReviewer>();
    }

    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = new ArrayList<Reviewer>();
            reviewers.add(devStatusReviewer);
        }
        return reviewers;
    }

    public List<ComparisonReviewer> getComparisonReviewers() {
        return comparisonReviewers;
    }
}
