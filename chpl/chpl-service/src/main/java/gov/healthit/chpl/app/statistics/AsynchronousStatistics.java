package gov.healthit.chpl.app.statistics;

import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.statistics.DeveloperStatisticsDAO;
import gov.healthit.chpl.dao.statistics.ListingStatisticsDAO;
import gov.healthit.chpl.dao.statistics.SurveillanceStatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

@Component
@EnableAsync
public class AsynchronousStatistics {
	@Autowired DeveloperStatisticsDAO developerStatisticsDAO;
	@Autowired ListingStatisticsDAO listingStatisticsDAO;
	@Autowired SurveillanceStatisticsDAO surveillanceStatisticsDAO;
	
	/**
	 * Total # of Unique Developers (Regardless of Edition) 
	 */
	@Transactional
	@Async
	public Future<Long> getTotalDevelopers(DateRange dateRange) {
		return new AsyncResult<>(developerStatisticsDAO.getTotalDevelopers(dateRange));
	}

	/**
	 * Total # of Developers with 2014 Listings
	 */
	@Transactional
	@Async
	public Future<Long> getTotalDevelopersWith2014Listings(DateRange dateRange) {
		return new AsyncResult<>(developerStatisticsDAO.getTotalDevelopersWith2014Listings(dateRange));
	}
	
	/**
	 * Total # of Developers by certified body with listings for each year
	 */
	@Transactional
	@Async
	public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsEachYear(DateRange dateRange) {
		return new AsyncResult<>(developerStatisticsDAO.getTotalDevelopersByCertifiedBodyWithListingsEachYear(dateRange));
	}
	
	/**
	 * Total # of Developers by certified body with listings in each certification status and year
	 */
	@Transactional
	@Async
	public Future<List<CertifiedBodyStatistics>> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(DateRange dateRange) {
		return new AsyncResult<>(developerStatisticsDAO.getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(dateRange));
	}

	/**
	 * Total # of Developers with 2015 Listings
	 */
	@Transactional
	@Async
	public Future<Long> getTotalDevelopersWith2015Listings(DateRange dateRange) {
		return new AsyncResult<>(developerStatisticsDAO.getTotalDevelopersWith2015Listings(dateRange));
	}

	/**
	 * Total # of Certified Unique Products (Regardless of Status or Edition - Including 2011) 
	 */
	@Async
	@Transactional
	public Future<Long> getTotalCertifiedProducts(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotalCertifiedProducts(dateRange));
	}
	
	/**
	 * Total # of unique Products with Active (Including Suspended) 2014 Listings
	 */
	@Async
	@Transactional
	public Future<Long> getTotalCPsActive2014Listings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotalCPsActive2014Listings(dateRange));
	}

	/**
	 * Total # of unique Products with Active (Including Suspended) 2015 Listings
	 */
	@Async
	@Transactional
	public Future<Long> getTotalCPsActive2015Listings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotalCPsActive2015Listings(dateRange));
	}

	/**
	 * Total # of unique Products with Active Listings (Including Suspended) (Regardless of Edition)
	 */
	@Async
	@Transactional
	public Future<Long> getTotalCPsActiveListings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotalCPsActiveListings(dateRange));
	}

	/**
	 * Total # of Listings (Regardless of Status or Edition) 
	 */
	@Async
	@Transactional
	public Future<Long> getTotalListings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotalListings(dateRange));
	}

	/**
	 * Total # of Active (Including Suspended) 2014 Listings
	 */
	@Async
	@Transactional
	public Future<Long> getTotalActive2014Listings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotalActive2014Listings(dateRange));
	}

	/**
	 * Total # of Active (Including Suspended) 2015 Listings
	 */
	@Async
	@Transactional
	public Future<Long> getTotalActive2015Listings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotalActive2015Listings(dateRange));
	}
	
	/**
	 * Total # of Active (Including Suspended) Listings by Certified Body
	 */
	@Async
	@Transactional
	public Future<List<CertifiedBodyStatistics>> getTotalActiveListingsByCertifiedBody(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotalActiveListingsByCertifiedBody(dateRange));
	}

	/**
	 * Total # of 2014 Listings (Regardless of Status)
	 */
	@Async
	@Transactional
	public Future<Long> getTotal2014Listings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotal2014Listings(dateRange));
	}

	/**
	 * Total # of 2015 Listings (Regardless of Status)
	 */
	@Async
	@Transactional
	public Future<Long> getTotal2015Listings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotal2015Listings(dateRange));
	}

	/**
	 * Total # of 2011 Listings (Will not be active)
	 */
	@Async
	@Transactional
	public Future<Long> getTotal2011Listings(DateRange dateRange) {
		return new AsyncResult<>(listingStatisticsDAO.getTotal2011Listings(dateRange));
	}

	/**
	 * Total # of Surveillance Activities* 
	 */
	@Async
	@Transactional
	public Future<Long> getTotalSurveillanceActivities(DateRange dateRange) {
		return new AsyncResult<>(surveillanceStatisticsDAO.getTotalSurveillanceActivities(dateRange));
	}

	/**
	 * Open Surveillance Activities
	 */
	@Async
	@Transactional
	public Future<Long> getTotalOpenSurveillanceActivities(DateRange dateRange) {
		return new AsyncResult<>(surveillanceStatisticsDAO.getTotalOpenSurveillanceActivities(dateRange));
	}

	/**
	 * Closed Surveillance Activities
	 */
	@Async
	@Transactional
	public Future<Long> getTotalClosedSurveillanceActivities(DateRange dateRange) {
		return new AsyncResult<>(surveillanceStatisticsDAO.getTotalClosedSurveillanceActivities(dateRange));
	}

	/**
	 * Total # of NCs
	 */
	@Async
	@Transactional
	public Future<Long> getTotalNonConformities(DateRange dateRange) {
		return new AsyncResult<>(surveillanceStatisticsDAO.getTotalNonConformities(dateRange));
	}

	/**
	 * Open NCs
	 */
	@Async
	@Transactional
	public Future<Long> getTotalOpenNonconformities(DateRange dateRange) {
		return new AsyncResult<>(surveillanceStatisticsDAO.getTotalOpenNonconformities(dateRange));
	}

	/**
	 * Closed NCs
	 */
	@Async
	@Transactional
	public Future<Long> getTotalClosedNonconformities(DateRange dateRange) {
		return new AsyncResult<>(surveillanceStatisticsDAO.getTotalClosedNonconformities(dateRange));
	}

}
