package gov.healthit.chpl.domain;

public class ListingUpdateRequest implements ExplainableAction {
    private CertifiedProductSearchDetails listing;
    private Boolean banDeveloper;
    private String reason;

    public CertifiedProductSearchDetails getListing() {
        return listing;
    }

    public void setListing(final CertifiedProductSearchDetails listing) {
        this.listing = listing;
    }

    public Boolean getBanDeveloper() {
        return banDeveloper;
    }

    public void setBanDeveloper(final Boolean banDeveloper) {
        this.banDeveloper = banDeveloper;
    }

    @Override
    public String getReason() {
        return this.reason;
    }

    @Override
    public void setReason(String reason) {
        this.reason = reason;
    }
}
