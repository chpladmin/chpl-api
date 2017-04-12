package gov.healthit.chpl.dao.impl;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.StatisticsDAO;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.Statistics;

@Repository("statisticsDAO")
public class StatisticsDAOImpl extends BaseDAOImpl implements StatisticsDAO {

	/**
	 * Total # of Unique Developers (Regardless of Edition) 
	 */
	@Override
	public Long getTotalDevelopers(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM DeveloperEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of Developers with 2014 Listings
	 */
	@Override
	public Long getTotalDevelopersWith2014Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(DISTINCT developerCode) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2014' AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate)"
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of Developers with 2015 Listings
	 */
	@Override
	public Long getTotalDevelopersWith2015Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(DISTINCT developerCode) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2015' AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate)"
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of Certified Unique Products (Regardless of Status or Edition – Including 2011) 
	 */
	@Override
	public Long getTotalCertifiedProducts(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(productName, developerName) FROM CertifiedProductDetailsEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}

	/**
	 * Total # of “unique” Products with Active (Including Suspended) 2014 Listings
	 */
	@Override
	public Long getTotalCPsActive2014Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(productName, developerName) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2014' AND UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}

	/**
	 * Total # of “unique” Products with Active (Including Suspended) 2015 Listings
	 */
	@Override
	public Long getTotalCPsActive2015Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(productName, developerName) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2015' AND UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}

	/**
	 * Total # of “unique” Products with Active Listings (Including Suspended) (Regardless of Edition)
	 */
	@Override
	public Long getTotalCPsActiveListings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(productName, developerName) FROM CertifiedProductDetailsEntity "
				+ " WHERE UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}

	/**
	 * Total # of Listings (Regardless of Status or Edition) 
	 */
	@Override
	public Long getTotalListings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM CertifiedProductDetailsEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of Active (Including Suspended) 2014 Listings
	 */
	@Override
	public Long getTotalActive2014Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2014' AND UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of Active (Including Suspended) 2015 Listings
	 */
	@Override
	public Long getTotalActive2015Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2015' AND UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of 2014 Listings (Regardless of Status)
	 */
	@Override
	public Long getTotal2014Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2014' AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of 2015 Listings (Regardless of Status)
	 */
	@Override
	public Long getTotal2015Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2015' AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of 2011 Listings (Will not be active)
	 */
	@Override
	public Long getTotal2011Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2011' AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of Surveillance Activities* 
	 */
	@Override
	public Long getTotalSurveillanceActivities(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM SurveillanceEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Open Surveillance Activities
	 */
	@Override
	public Long getTotalOpenSurveillanceActivities(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM SurveillanceEntity "
				+ " WHERE startDate <= now() AND (endDate IS NULL OR endDate >= now()) AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Closed Surveillance Activities
	 */
	@Override
	public Long getTotalClosedSurveillanceActivities(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM SurveillanceEntity "
				+ " WHERE startDate <= now() AND (endDate IS NOT NULL AND endDate <= now()) AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of NCs
	 */
	@Override
	public Long getTotalNonConformities(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM SurveillanceNonconformityEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Open NCs
	 */
	@Override
	public Long getTotalOpenNonconformities(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM SurveillanceNonconformityEntity "
				+ " WHERE nonconformityStatusId = 1 AND ((deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate))) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Closed NCs
	 */
	@Override
	public Long getTotalClosedNonconformities(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(*) FROM SurveillanceNonconformityEntity "
				+ " WHERE nonconformityStatusId = 2 AND ((deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate))) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	@Override
	public Statistics calculateStatistics(DateRange dateRange) {
		return new Statistics(getTotalDevelopers(dateRange), getTotalDevelopersWith2014Listings(dateRange), getTotalDevelopersWith2015Listings(dateRange),
				getTotalCertifiedProducts(dateRange), getTotalCPsActive2014Listings(dateRange), getTotalCPsActive2015Listings(dateRange),
				getTotalCPsActiveListings(dateRange), getTotalListings(dateRange), getTotalActive2014Listings(dateRange), getTotalActive2015Listings(dateRange),
				getTotal2014Listings(dateRange), getTotal2015Listings(dateRange), getTotal2011Listings(dateRange), getTotalSurveillanceActivities(dateRange),
				getTotalOpenSurveillanceActivities(dateRange), getTotalClosedSurveillanceActivities(dateRange), getTotalNonConformities(dateRange),
				getTotalOpenNonconformities(dateRange), getTotalClosedNonconformities(dateRange));
	}

}
