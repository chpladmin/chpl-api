package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

/**
 * Interface for database access to developer statistics.
 * @author kekey
 *
 */
public interface DeveloperStatisticsDAO {
    Long getTotalDevelopers(DateRange dateRange);
    Long getTotalDevelopersWithListingsByEditionAndStatus(DateRange dateRange, String edition, List<String> statuses);

    List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear(DateRange dateRange);

    List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
            DateRange dateRange);
}
