package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

public interface ListingStatisticsDAO {
    Long getTotalListings(DateRange dateRange);

    List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody(DateRange dateRange);

    List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody(DateRange dateRange);

    List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            DateRange dateRange);

    Long getTotalCertifiedProducts(DateRange dateRange);

    Long getTotalCPsActiveListings(DateRange dateRange);

    // 2011
    Long getTotal2011Listings(DateRange dateRange);

    // 2014
    Long getTotalCPs2014Listings(DateRange dateRange);

    Long getTotalActive2014Listings(DateRange dateRange);

    Long getTotalCPsSuspended2014Listings(DateRange dateRange);

    Long getTotal2014Listings(DateRange dateRange);

    Long getTotalCPsActive2014Listings(DateRange dateRange);

    // 2015
    Long getTotalCPs2015Listings(DateRange dateRange);

    Long getTotal2015Listings(DateRange dateRange);

    Long getTotalActive2015Listings(DateRange dateRange);

    Long getTotalCPsSuspended2015Listings(DateRange dateRange);

    Long getTotalCPsActive2015Listings(DateRange dateRange);

}
