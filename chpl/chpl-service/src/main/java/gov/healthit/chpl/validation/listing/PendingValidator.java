package gov.healthit.chpl.validation.listing;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import gov.healthit.chpl.dao.PendingCertifiedProductDAO;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;
import gov.healthit.chpl.validation.pendingListing.reviewer.Reviewer;

/**
 * Abstract class for validating Pending Listings.
 * @author alarned
 *
 */
public abstract class PendingValidator {
    private static final Logger LOGGER = LogManager.getLogger(PendingValidator.class);

    @Autowired
    private PendingCertifiedProductDAO pendingListingDao;

    /**
     * Concrete classes should provide a list of reviewers.
     * Each reviewer can check a specific part of a listing for errors.
     * @return the list of applicable reviewers
     */
    public abstract List<Reviewer> getReviewers();

    public void validate(final PendingCertifiedProductDTO listing) {
        validate(listing, true);
    }

    /**
     * Validation simply calls each reviewer. The reviewers add
     * errors and warnings as appropriate.
     * @param listing a pending listing
     */
    public void validate(final PendingCertifiedProductDTO listing, boolean updateCounts) {
        for (Reviewer reviewer : getReviewers()) {
            reviewer.review(listing);
        }
        if (updateCounts) {
            updateCounts(listing);
        }
    }

    /**
     * Updates error and warning counts
     * @param listing a pending listing
     */
    public void updateCounts(PendingCertifiedProductDTO listing) {
        int errorCount = (listing.getErrorMessages() == null ? 0 : listing.getErrorMessages().size());
        int warningCount = (listing.getWarningMessages() == null ? 0 : listing.getWarningMessages().size());
        try {
            pendingListingDao.updateErrorAndWarningCounts(listing.getId(), errorCount, warningCount);
        } catch (Exception ex) {
            LOGGER.error("Unable to update error and warning counts for pending listing " + listing.getId(), ex);
        }
    }
}
