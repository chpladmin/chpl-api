package gov.healthit.chpl.validation.listing;

import java.util.List;

import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

/**
 * Abstract class for validating Pending Listings.
 * @author alarned
 *
 */
public abstract class PendingValidator {

    /**
     * Concrete classes should provide a list of reviewers.
     * Each reviewer can check a specific part of a listing for errors.
     * @return the list of applicable reviewers
     */
    public abstract List<Reviewer> getReviewers();

    /**
     * Validation simply calls each reviewer. The reviewers add
     * errors and warnings as appropriate.
     * @param listing a pending listing
     */
    public void validate(final PendingCertifiedProductDTO listing) {
        for (Reviewer reviewer : getReviewers()) {
            reviewer.review(listing);
        }
    }
}
