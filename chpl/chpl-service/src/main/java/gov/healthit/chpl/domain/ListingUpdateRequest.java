package gov.healthit.chpl.domain;

public class ListingUpdateRequest {
    private CertifiedProductSearchDetails listing;
    private Boolean banDeveloper;

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
}
