package gov.healthit.chpl.validation.pendingListing.reviewer;
import gov.healthit.chpl.dto.listing.pending.PendingCertifiedProductDTO;

/**
 * Interface for reviewers of pending Listings.
 * @author alarned
 *
 */
public interface Reviewer {
    /**
     * Review a pending listing.
     * @param listing the listing
     */
    void review(PendingCertifiedProductDTO listing);
}
