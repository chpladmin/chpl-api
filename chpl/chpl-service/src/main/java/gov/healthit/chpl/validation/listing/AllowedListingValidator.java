package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.CertificationStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.DeveloperStatusReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

@Component
public class AllowedListingValidator extends Validator {
    private List<Reviewer> reviewersToAlwaysCheck;
    private List<Reviewer> reviewers;
    private List<ComparisonReviewer> comparisonReviewers;

    @Autowired
    public AllowedListingValidator(@Qualifier("developerStatusReviewer") DeveloperStatusReviewer devStatusReviewer,
            @Qualifier("certificationStatusReviewer") CertificationStatusReviewer certStatusReviewer) {

        this.reviewersToAlwaysCheck = new ArrayList<Reviewer>();
        this.reviewersToAlwaysCheck.add(devStatusReviewer);
        this.reviewersToAlwaysCheck.add(certStatusReviewer);
        this.reviewers = new ArrayList<Reviewer>();
        this.comparisonReviewers = new ArrayList<ComparisonReviewer>();
    }

    public List<Reviewer> getReviewers() {
        return reviewers;
    }

    public List<Reviewer> getReviewersToAlwaysCheck() {
        return reviewersToAlwaysCheck;
    }

    public List<ComparisonReviewer> getComparisonReviewers() {
        return comparisonReviewers;
    }

    public List<ComparisonReviewer> getComparisonReviewersToAlwaysCheck() {
        return comparisonReviewers;
    }
}
