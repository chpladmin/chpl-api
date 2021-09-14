package gov.healthit.chpl.validation.listing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import gov.healthit.chpl.validation.listing.reviewer.CertificationDateComparisonReviewer;
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
    private CertificationDateComparisonReviewer certificationDateComparisonReviewer;

    private List<Reviewer> reviewers;
    private List<ComparisonReviewer> comparisonReviewers;

    @Autowired
    public AllowedListingValidator(@Qualifier("developerStatusReviewer") DeveloperStatusReviewer devStatusReviewer,
            @Qualifier("certificationDateComparisonReviewer") CertificationDateComparisonReviewer certificationDateComparisonReviewer) {
        this.devStatusReviewer = devStatusReviewer;
        this.certificationDateComparisonReviewer = certificationDateComparisonReviewer;
    }

    public List<Reviewer> getReviewers() {
        if (reviewers == null) {
            reviewers = new ArrayList<Reviewer>();
            reviewers.add(devStatusReviewer);
        }
        return reviewers;
    }

    public List<ComparisonReviewer> getComparisonReviewers() {
        if (comparisonReviewers == null) {
            comparisonReviewers = new ArrayList<ComparisonReviewer>();
            comparisonReviewers.add(certificationDateComparisonReviewer);
        }
        return comparisonReviewers;
    }
}
