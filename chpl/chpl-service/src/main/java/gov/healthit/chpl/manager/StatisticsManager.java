package gov.healthit.chpl.manager;

import gov.healthit.chpl.domain.DateRange;

public interface StatisticsManager {
	public Long getTotalDevelopers(DateRange dateRange);
	public Long getTotalDevelopersWith2014Listings(DateRange dateRange);
	public Long getTotalDevelopersWith2015Listings(DateRange dateRange);
	public Long getTotalCertifiedProducts(DateRange dateRange);
	public Long getTotalCPsActive2014Listings(DateRange dateRange);
	public Long getTotalCPsActive2015Listings(DateRange dateRange);
	public Long getTotalCPsActiveListings(DateRange dateRange);
	public Long getTotalListings(DateRange dateRange);
	public Long getTotalActive2014Listings(DateRange dateRange);
	public Long getTotalActive2015Listings(DateRange dateRange);
	public Long getTotal2014Listings(DateRange dateRange);
	public Long getTotal2015Listings(DateRange dateRange);
	public Long getTotal2011Listings(DateRange dateRange);
	public Long getTotalSurveillanceActivities(DateRange dateRange);
	public Long getTotalOpenSurveillanceActivities(DateRange dateRange);
	public Long getTotalClosedSurveillanceActivities(DateRange dateRange);
	public Long getTotalNonConformities(DateRange dateRange);
	public Long getTotalOpenNonconformities(DateRange dateRange);
	public Long getTotalClosedNonconformities(DateRange dateRange);
}
