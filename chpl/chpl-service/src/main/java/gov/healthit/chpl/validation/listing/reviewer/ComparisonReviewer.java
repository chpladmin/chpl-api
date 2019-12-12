package gov.healthit.chpl.validation.listing.reviewer;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

/**
 * Interface for ComparisonReviewer.
 * @author kekey
 *
 */
public interface ComparisonReviewer {
    /**
     * Compares the existing/updated listings for any
     * changes that may not be allowed.
     * @param existingListing the listing as it exists in the system
     * @param updatedListing the listing with updates made by the user
     */
    void review(CertifiedProductSearchDetails existingListing, CertifiedProductSearchDetails updatedListing);
}
