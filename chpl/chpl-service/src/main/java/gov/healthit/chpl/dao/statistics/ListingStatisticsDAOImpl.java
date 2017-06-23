package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

@Repository("listingStatisticsDAO")
public class ListingStatisticsDAOImpl extends BaseDAOImpl implements ListingStatisticsDAO {
	/**
	 * Total # of Certified Unique Products (Regardless of Status or Edition – Including 2011) 
	 */
	@Override
	public Long getTotalCertifiedProducts(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(UPPER(productName), UPPER(developerName)) FROM CertifiedProductDetailsEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}
	
	/**
	 * Total # of unique Products with 2014 Listings
	 */
	@Override
	public Long getTotalCPs2014Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(UPPER(productName), UPPER(developerName)) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2014' "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}
	
	/**
	 * Total # of unique Products by certified product with Listings in each year
	 */
	@Override
	public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBody(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT certificationBodyName, year, count(DISTINCT uniqueProduct) "
				+ " FROM CertifiedProductDetailsEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate) "
				+ " GROUP BY certificationBodyName, year "
				+ " ORDER BY certificationBodyName ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		List<Object[]> results = query.getResultList();
		List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
		for(Object[] obj : results){
			CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
			stat.setName(obj[0].toString());
			stat.setYear(Integer.valueOf(obj[1].toString()));
			stat.setTotalListings(Long.valueOf(obj[2].toString()));
			stat.setCertificationStatusName(null);
			cbStats.add(stat);
		}
		return cbStats;
	}
	
	/**
	 * Total # of unique Products by certified product with Listings in each year
	 */
	@Override
	public List<CertifiedBodyStatistics> getTotalCPListingsEachYearByCertifiedBodyAndCertificationStatus(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT certificationBodyName, year, count(DISTINCT uniqueProduct), certificationStatusName "
				+ " FROM CertifiedProductDetailsEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate) "
				+ " GROUP BY certificationBodyName, year, certificationStatusName "
				+ " ORDER BY certificationBodyName ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		List<Object[]> results = query.getResultList();
		List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
		for(Object[] obj : results){
			CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
			stat.setName(obj[0].toString());
			stat.setYear(Integer.valueOf(obj[1].toString()));
			stat.setTotalListings(Long.valueOf(obj[2].toString()));
			stat.setCertificationStatusName(obj[3].toString());
			cbStats.add(stat);
		}
		return cbStats;
	}

	/**
	 * Total # of unique Products with Active (Including Suspended) 2014 Listings
	 */
	@Override
	public Long getTotalCPsActive2014Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(UPPER(productName), UPPER(developerName)) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2014' AND UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}
	
	/**
	 * Total # of unique Products with Suspended (by ONC or ONC-ACB) 2014 Listings
	 */
	@Override
	public Long getTotalCPsSuspended2014Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(UPPER(productName), UPPER(developerName)) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2014' AND UPPER(certificationStatusName) IN ('SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}
	
	/**
	 * Total # of unique Products with 2015 Listings
	 */
	@Override
	public Long getTotalCPs2015Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(UPPER(productName), UPPER(developerName)) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2015' "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}

	/**
	 * Total # of unique Products with Active (Including Suspended) 2015 Listings
	 */
	@Override
	public Long getTotalCPsActive2015Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(UPPER(productName), UPPER(developerName)) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2015' AND UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}
	
	/**
	 * Total # of unique Products with Suspended (by ONC or ONC-ACB) 2015 Listings
	 */
	@Override
	public Long getTotalCPsSuspended2015Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(UPPER(productName), UPPER(developerName)) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2015' AND UPPER(certificationStatusName) IN ('SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (long) query.getResultList().size();
	}

	/**
	 * Total # of unique Products with Active Listings (Including Suspended) (Regardless of Edition)
	 */
	@Override
	public Long getTotalCPsActiveListings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT DISTINCT CONCAT(UPPER(productName), UPPER(developerName)) FROM CertifiedProductDetailsEntity "
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
	 * Total # of Active (Including Suspended) Listings in each year
	 */
	@Override
	public List<CertifiedBodyStatistics> getTotalActiveListingsByCertifiedBody(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT certificationBodyName, year, count(*) FROM CertifiedProductDetailsEntity "
				+ " WHERE UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') "
				+ " AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate) "
				+ " GROUP BY certificationBodyName, year "
				+ " ORDER BY certificationBodyName ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		List<Object[]> results = query.getResultList();
		List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
		for(Object[] obj : results){
			CertifiedBodyStatistics stat = new CertifiedBodyStatistics();
			stat.setName(obj[0].toString());
			stat.setYear(Integer.valueOf(obj[1].toString()));
			stat.setTotalListings(Long.valueOf(obj[2].toString()));
			stat.setCertificationStatusName(null);
			cbStats.add(stat);
		}
		return cbStats;
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
}
