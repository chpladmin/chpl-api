package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

public interface DeveloperStatisticsDAO {
    Long getTotalDevelopers(DateRange dateRange);

    // 2014
    Long getTotalDevelopersWith2014Listings(DateRange dateRange);

    Long getTotalDevelopersWithActive2014Listings(DateRange dateRange);

    List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear(DateRange dateRange);

    List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(
            DateRange dateRange);

    // 2015
    Long getTotalDevelopersWith2015Listings(DateRange dateRange);

    Long getTotalDevelopersWithActive2015Listings(DateRange dateRange);
}
