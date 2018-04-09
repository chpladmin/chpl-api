package gov.healthit.chpl.domain.statistics;

/**
 * Domain objects for holding Certification Bodies and the number of Listings that have Alternative Test Methods.
 * @author alarned
 *
 */
public class CertifiedBodyAltTestStatistics extends Statistic {
    private Long totalListings;
    private Long totalDevelopersWithListings;

    /** Default constructor. */
    public CertifiedBodyAltTestStatistics() {
    }

    public Long getTotalListings() {
        return totalListings;
    }

    public void setTotalListings(final Long totalListings) {
        this.totalListings = totalListings;
    }

    public Long getTotalDevelopersWithListings() {
        return totalDevelopersWithListings;
    }

    public void setTotalDevelopersWithListings(final Long totalDevelopersWithListings) {
        this.totalDevelopersWithListings = totalDevelopersWithListings;
    }
}
