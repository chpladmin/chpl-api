package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyAltTestStatistics;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

/**
 * Interface for getting statistics about Listings.
 * @author alarned
 *
 */
public interface ListingStatisticsDAO {
    /**
     * Retrieve a filtered count of listings.
     * @param dateRange date range to search in
     * @param edition edition to filter on
     * @param statuses statuses to filter on
     * @return a number
     */
    Long getTotalListingsByEditionAndStatus(DateRange dateRange, String edition, List<String> statuses);
    /**
     * Return counts of active listings broken up by ACB.
     * @param dateRange range to search in
     * @return counts of listings
     */
    List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody(DateRange dateRange);
    /**
     * Return total unique products filtered.
     * @param dateRange range to search in
     * @param edition edition to filter on
     * @param statuses status to filter on
     * @return the count
     */
    Long getTotalUniqueProductsByEditionAndStatus(DateRange dateRange, String edition, List<String> statuses);
    /**
     * Retrieve the total counts of Listings broken out by ACB & edition.
     * @param dateRange range to search in
     * @return statistics
     */
    List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody(DateRange dateRange);
    /**
     * Retrieve the total counts of Listings broken out by ACB, edition, and status.
     * @param dateRange range to search in
     * @return statistics
     */
    List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(
            DateRange dateRange);
    /**
     * Retrieve the count of Listings that have Alternate Test Methods.
     * @return the count
     */
    Long getTotalListingsWithAlternateTestMethods();
    /**
     * Retrieve the count of Listings with Alternate Test Methods broken out by ACB.
     * @return statistics
     */
    List<CertifiedBodyAltTestStatistics> getTotalListingsWithCertifiedBodyAndAlternativeTestMethods();
}
