package gov.healthit.chpl.validation.listing;

import java.util.List;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;

public abstract class Validator {
    
    /**
     * Concrete classes should provide a list of reviewers.
     * Each reviewer can check a specific part of a listing for errors.
     * @return
     */
    public abstract List<Reviewer> getReviewers();

    /**
     * Validation simply calls each reviewer. The reviewers add 
     * errors and warnings as appropriate.
     * @param listing
     */
    public void validate(final CertifiedProductSearchDetails listing) {
        for(Reviewer reviewer : getReviewers()) {
            reviewer.review(listing);
        }
    }
}
