package gov.healthit.chpl.app.statistics;

import java.util.List;
import java.util.concurrent.Future;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import gov.healthit.chpl.dao.StatisticsDAO;
import gov.healthit.chpl.domain.CertifiedBodyStatistics;
import gov.healthit.chpl.domain.DateRange;

@Component
@EnableAsync
public class AsynchronousStatistics {
	@Autowired StatisticsDAO statisticsDAO;
	
	/**
	 * Total # of Unique Developers (Regardless of Edition) 
	 */
	@Transactional
	@Async
	public Future<Long> getTotalDevelopers(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalDevelopers(dateRange));
	}

	/**
	 * Total # of Developers with 2014 Listings
	 */
	@Transactional
	@Async
	public Future<Long> getTotalDevelopersWith2014Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalDevelopersWith2014Listings(dateRange));
	}

	/**
	 * Total # of Developers with 2015 Listings
	 */
	@Transactional
	@Async
	public Future<Long> getTotalDevelopersWith2015Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalDevelopersWith2015Listings(dateRange));
	}

	/**
	 * Total # of Certified Unique Products (Regardless of Status or Edition – Including 2011) 
	 */
	@Async
	@Transactional
	public Future<Long> getTotalCertifiedProducts(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalCertifiedProducts(dateRange));
	}
	
	/**
	 * Total # of “unique” Products with Active (Including Suspended) 2014 Listings
	 */
	@Async
	@Transactional
	public Future<Long> getTotalCPsActive2014Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalCPsActive2014Listings(dateRange));
	}

	/**
	 * Total # of “unique” Products with Active (Including Suspended) 2015 Listings
	 */
	@Async
	@Transactional
	public Future<Long> getTotalCPsActive2015Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalCPsActive2015Listings(dateRange));
	}

	/**
	 * Total # of “unique” Products with Active Listings (Including Suspended) (Regardless of Edition)
	 */
	@Async
	@Transactional
	public Future<Long> getTotalCPsActiveListings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalCPsActiveListings(dateRange));
	}

	/**
	 * Total # of Listings (Regardless of Status or Edition) 
	 */
	@Async
	@Transactional
	public Future<Long> getTotalListings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalListings(dateRange));
	}

	/**
	 * Total # of Active (Including Suspended) 2014 Listings
	 */
	@Async
	@Transactional
	public Future<Long> getTotalActive2014Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalActive2014Listings(dateRange));
	}

	/**
	 * Total # of Active (Including Suspended) 2015 Listings
	 */
	@Async
	@Transactional
	public Future<Long> getTotalActive2015Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalActive2015Listings(dateRange));
	}
	
	/**
	 * Total # of Active (Including Suspended) Listings by Certified Body
	 */
	@Async
	@Transactional
	public Future<List<CertifiedBodyStatistics>> getTotalActiveListingsByCertifiedBody(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalActiveListingsByCertifiedBody(dateRange));
	}

	/**
	 * Total # of 2014 Listings (Regardless of Status)
	 */
	@Async
	@Transactional
	public Future<Long> getTotal2014Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotal2014Listings(dateRange));
	}

	/**
	 * Total # of 2015 Listings (Regardless of Status)
	 */
	@Async
	@Transactional
	public Future<Long> getTotal2015Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotal2015Listings(dateRange));
	}

	/**
	 * Total # of 2011 Listings (Will not be active)
	 */
	@Async
	@Transactional
	public Future<Long> getTotal2011Listings(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotal2011Listings(dateRange));
	}

	/**
	 * Total # of Surveillance Activities* 
	 */
	@Async
	@Transactional
	public Future<Long> getTotalSurveillanceActivities(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalSurveillanceActivities(dateRange));
	}

	/**
	 * Open Surveillance Activities
	 */
	@Async
	@Transactional
	public Future<Long> getTotalOpenSurveillanceActivities(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalOpenSurveillanceActivities(dateRange));
	}

	/**
	 * Closed Surveillance Activities
	 */
	@Async
	@Transactional
	public Future<Long> getTotalClosedSurveillanceActivities(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalClosedSurveillanceActivities(dateRange));
	}

	/**
	 * Total # of NCs
	 */
	@Async
	@Transactional
	public Future<Long> getTotalNonConformities(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalNonConformities(dateRange));
	}

	/**
	 * Open NCs
	 */
	@Async
	@Transactional
	public Future<Long> getTotalOpenNonconformities(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalOpenNonconformities(dateRange));
	}

	/**
	 * Closed NCs
	 */
	@Async
	@Transactional
	public Future<Long> getTotalClosedNonconformities(DateRange dateRange) {
		return new AsyncResult<>(statisticsDAO.getTotalClosedNonconformities(dateRange));
	}

}
