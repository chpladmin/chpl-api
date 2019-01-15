package gov.healthit.chpl.validation.pendingListing.reviewer;
import gov.healthit.chpl.dto.PendingCertifiedProductDTO;
import gov.healthit.chpl.exception.ValidationException;

/**
 * Interface for reviewers of pending Listings.
 * @author alarned
 *
 */
public interface Reviewer {
    /**
     * Review a pending listing.
     * @param listing the listing
     * @throws ValidationException 
     */
    void review(PendingCertifiedProductDTO listing) throws ValidationException;
}
