package gov.healthit.chpl.validation.listing;

import java.util.List;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;
import gov.healthit.chpl.validation.listing.reviewer.ComparisonReviewer;
import gov.healthit.chpl.validation.listing.reviewer.Reviewer;
import lombok.extern.log4j.Log4j2;

@Log4j2
public abstract class Validator {

    /**
     * Concrete classes should provide a list of reviewers.
     * Each reviewer can check a specific part of a listing for errors.
     * @return the applicable reviewers
     */
    public abstract List<Reviewer> getReviewers();

    /**
     * Concrete classes should provide a list of comparison reviewers, if any.
     * Each comparison reviewer can compare existing listing data against
     * proposed updated listing data and generate error/warning messages.
     * @return the applicable reviewers
     */
    public abstract List<ComparisonReviewer> getComparisonReviewers();

    /**
     * Validation simply calls each reviewer. The reviewers add
     * errors and warnings as appropriate.
     * @param listing the listing to validate
     */
    public void validate(final CertifiedProductSearchDetails listing) {
        for (Reviewer reviewer : getReviewers()) {
            try {
                if (reviewer != null) {
                    LOGGER.info("Running Reviewer: " + reviewer.getClass().getName());
                    reviewer.review(listing);
                } else {
                    LOGGER.info("Cound not run a NULL reviewer.");
                }
            } catch (Exception e) {
                LOGGER.error("There was an exception trying to run the Reviewer: " + reviewer.getClass().getName());
                throw e;
            }
        }
    }

    /**
     * Validates the updated listing.
     * Does additional validation to check whether certain changes were made
     * between the existing and updated listings that are only allowed
     * by certain users.
     * @param existingListing the listing as it currently exists in the system
     * @param updatedListing the listing with any updates made by the user
     */
    public void validate(final CertifiedProductSearchDetails existingListing,
            final CertifiedProductSearchDetails updatedListing) {
        validate(updatedListing);
        for (ComparisonReviewer reviewer : getComparisonReviewers()) {
            reviewer.review(existingListing, updatedListing);
        }
    }
}
