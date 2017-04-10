package gov.healthit.chpl.dao;

import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.Statistics;

public interface StatisticsDAO {
	public long getTotalDevelopers(DateRange dateRange);
	public long getTotalDevelopersWith2014Listings(DateRange dateRange);
	public long getTotalDevelopersWith2015Listings(DateRange dateRange);
	public long getTotalCertifiedProducts(DateRange dateRange);
	public long getTotalCPsActive2014Listings(DateRange dateRange);
	public long getTotalCPsActive2015Listings(DateRange dateRange);
	public long getTotalCPsActiveListings(DateRange dateRange);
	public long getTotalListings(DateRange dateRange);
	public long getTotalActive2014Listings(DateRange dateRange);
	public long getTotalActive2015Listings(DateRange dateRange);
	public long getTotal2014Listings(DateRange dateRange);
	public long getTotal2015Listings(DateRange dateRange);
	public long getTotal2011Listings(DateRange dateRange);
	public long getTotalSurveillanceActivities(DateRange dateRange);
	public long getTotalOpenSurveillanceActivities(DateRange dateRange);
	public long getTotalClosedSurveillanceActivities(DateRange dateRange);
	public long getTotalNonConformities(DateRange dateRange);
	public long getTotalOpenNonconformities(DateRange dateRange);
	public long getTotalClosedNonconformities(DateRange dateRange);
	public Statistics calculateStatistics(DateRange dateRange);
}
