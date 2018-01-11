package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

public interface ListingStatisticsDAO {
    public Long getTotalListingsByEditionAndStatus(DateRange dateRange, String edition, List<String> statuses);
    
    List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody(DateRange dateRange);

    public Long getTotalUniqueProductsByEditionAndStatus(DateRange dateRange, String edition, List<String> statuses);
    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody(DateRange dateRange);
    public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            DateRange dateRange);
}
