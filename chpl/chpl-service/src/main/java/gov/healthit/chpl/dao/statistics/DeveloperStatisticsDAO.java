package gov.healthit.chpl.dao.statistics;

import java.util.List;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

public interface DeveloperStatisticsDAO {
	public Long getTotalDevelopers(DateRange dateRange);
	// 2014
	public Long getTotalDevelopersWith2014Listings(DateRange dateRange);
	public Long getTotalDevelopersWithActive2014Listings(DateRange dateRange);
	public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear(DateRange dateRange);
	public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(DateRange dateRange);
	// 2015
	public Long getTotalDevelopersWith2015Listings(DateRange dateRange);
	public Long getTotalDevelopersWithActive2015Listings(DateRange dateRange);
}
