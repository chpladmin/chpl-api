package gov.healthit.chpl.domain;

/**
 * Object containing needed data for updating a Listing.
 * @author alarned
 *
 */
public class ListingUpdateRequest implements ExplainableAction {
    private CertifiedProductSearchDetails listing;
    private String reason;

    public CertifiedProductSearchDetails getListing() {
        return listing;
    }

    public void setListing(final CertifiedProductSearchDetails listing) {
        this.listing = listing;
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    @Override
    public void setReason(final String reason) {
        this.reason = reason;
    }
}
