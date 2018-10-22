package gov.healthit.chpl.validation.listing.reviewer;

import gov.healthit.chpl.domain.CertifiedProductSearchDetails;

/**
 * Interface for Reviewers.
 * @author alarned
 *
 */
public interface Reviewer {
    /**
     * Review a Listing.
     * @param listing to review
     */
    void review(CertifiedProductSearchDetails listing);
}
