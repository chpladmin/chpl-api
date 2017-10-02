package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

public interface ListingStatisticsDAO {
    public Long getTotalListings(DateRange dateRange);

    public List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody(DateRange dateRange);

    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody(DateRange dateRange);

    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            DateRange dateRange);

    public Long getTotalCertifiedProducts(DateRange dateRange);

    public Long getTotalCPsActiveListings(DateRange dateRange);

    // 2011
    public Long getTotal2011Listings(DateRange dateRange);

    // 2014
    public Long getTotalCPs2014Listings(DateRange dateRange);

    public Long getTotalActive2014Listings(DateRange dateRange);

    public Long getTotalCPsSuspended2014Listings(DateRange dateRange);

    public Long getTotal2014Listings(DateRange dateRange);

    public Long getTotalCPsActive2014Listings(DateRange dateRange);

    // 2015
    public Long getTotalCPs2015Listings(DateRange dateRange);

    public Long getTotal2015Listings(DateRange dateRange);

    public Long getTotalActive2015Listings(DateRange dateRange);

    public Long getTotalCPsSuspended2015Listings(DateRange dateRange);

    public Long getTotalCPsActive2015Listings(DateRange dateRange);

}
