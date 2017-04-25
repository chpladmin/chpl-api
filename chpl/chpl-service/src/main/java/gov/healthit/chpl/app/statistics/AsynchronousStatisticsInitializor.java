package gov.healthit.chpl.app.statistics;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.domain.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.Statistics;

@Repository("asynchronousStatisticsInitializor")
@EnableAsync
public class AsynchronousStatisticsInitializor {
	private static final Logger logger = LogManager.getLogger(AsynchronousStatisticsInitializor.class);
	@Autowired private AsynchronousStatistics asyncStats;
	
	@Transactional
	@Async
	public Future<Statistics> getStatistics(DateRange dateRange, Boolean includeActiveStatistics) throws InterruptedException, ExecutionException{
		logger.info("Getting statistics for start date " + dateRange.getStartDate() + " end date " + dateRange.getEndDate());
		Statistics stats = new Statistics();
		stats.setDateRange(dateRange);
		Future<Long> totalActive2014Listings = null;
		Future<Long> totalActive2015Listings = null;
		Future<List<CertifiedBodyStatistics>> totalActiveListingsByCertifiedBody = null;
		
		if(includeActiveStatistics){
			totalActive2014Listings = asyncStats.getTotalActive2014Listings(dateRange);
			totalActive2015Listings = asyncStats.getTotalActive2015Listings(dateRange);
			totalActiveListingsByCertifiedBody = asyncStats.getTotalActiveListingsByCertifiedBody(dateRange);
 		}
		Future<Long> totalDevelopers = asyncStats.getTotalDevelopers(dateRange);
		Future<Long> totalDevelopersWith2014Listings = asyncStats.getTotalDevelopersWith2014Listings(dateRange);
		Future<Long> totalDeveloperswith2015Listings = asyncStats.getTotalDevelopersWith2015Listings(dateRange);
		Future<Long> totalCertifiedProducts = asyncStats.getTotalCertifiedProducts(dateRange);
		Future<Long> totalCPsActive2014Listings = asyncStats.getTotalCPsActive2014Listings(dateRange);
		Future<Long> totalCPsActive2015Listings = asyncStats.getTotalCPsActive2015Listings(dateRange);
		Future<Long> totalCPsActiveListings = asyncStats.getTotalCPsActiveListings(dateRange);
		Future<Long> totalListings = asyncStats.getTotalListings(dateRange);
		Future<Long> total2014Listings = asyncStats.getTotal2014Listings(dateRange);
		Future<Long> total2015Listings = asyncStats.getTotal2015Listings(dateRange);
		Future<Long> total2011Listings = asyncStats.getTotal2011Listings(dateRange);
		Future<Long> totalSurveillanceActivities = asyncStats.getTotalSurveillanceActivities(dateRange);
		Future<Long> totalOpenSurveillanceActivities = asyncStats.getTotalOpenSurveillanceActivities(dateRange);
		Future<Long> totalClosedSurveillanceActivities = asyncStats.getTotalClosedSurveillanceActivities(dateRange);
		Future<Long> totalNonConformities = asyncStats.getTotalNonConformities(dateRange);
		Future<Long> totalOpenNonConformities = asyncStats.getTotalOpenNonconformities(dateRange);
		Future<Long> totalClosedNonConformities = asyncStats.getTotalClosedNonconformities(dateRange);
		
		if(includeActiveStatistics){
			stats.setTotalActive2014Listings(totalActive2014Listings.get());
			stats.setTotalActive2015Listings(totalActive2015Listings.get());
			stats.setTotalActiveListingsByCertifiedBody(totalActiveListingsByCertifiedBody.get());
 		}
		stats.setTotalDevelopers(totalDevelopers.get());
		stats.setTotalDevelopersWith2014Listings(totalDevelopersWith2014Listings.get());
		stats.setTotalDevelopersWith2015Listings(totalDeveloperswith2015Listings.get());
		stats.setTotalCertifiedProducts(totalCertifiedProducts.get());
		stats.setTotalCPsActiveListings(totalCPsActiveListings.get());
		stats.setTotalCPsActive2014Listings(totalCPsActive2014Listings.get());
		stats.setTotalCPsActive2015Listings(totalCPsActive2015Listings.get());
		stats.setTotalListings(totalListings.get());
		stats.setTotal2014Listings(total2014Listings.get());
		stats.setTotal2015Listings(total2015Listings.get());
		stats.setTotal2011Listings(total2011Listings.get());
		stats.setTotalSurveillanceActivities(totalSurveillanceActivities.get());
		stats.setTotalOpenSurveillanceActivities(totalOpenSurveillanceActivities.get());
		stats.setTotalClosedSurveillanceActivities(totalClosedSurveillanceActivities.get());
		stats.setTotalNonConformities(totalNonConformities.get());
		stats.setTotalOpenNonconformities(totalOpenNonConformities.get());
		stats.setTotalClosedNonconformities(totalClosedNonConformities.get());
		
		return new AsyncResult<>(stats);
	}
	
}
