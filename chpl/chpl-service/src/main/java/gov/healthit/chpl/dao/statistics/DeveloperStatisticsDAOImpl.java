package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.statistics.CertifiedBodyStatistics;

@Repository("developerStatisticsDAO")
public class DeveloperStatisticsDAOImpl extends BaseDAOImpl implements DeveloperStatisticsDAO {

	/**
	 * Total # of Unique Developers (Regardless of Edition) 
	 */
	@Override
	public Long getTotalDevelopers(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(DISTINCT developerCode) FROM DeveloperEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}

	/**
	 * Total # of Developers with Active 2014 Listings
	 */
	@Override
	public Long getTotalDevelopersWithActive2014Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(DISTINCT developerCode) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2014' AND UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate)"
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
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
	 * Total # of Developers with Active 2015 Listings
	 */
	@Override
	public Long getTotalDevelopersWithActive2015Listings(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT count(DISTINCT developerCode) FROM CertifiedProductDetailsEntity "
				+ " WHERE year = '2015' AND UPPER(certificationStatusName) IN ('ACTIVE', 'SUSPENDED BY ONC-ACB', 'SUSPENDED BY ONC') AND (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate)"
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate)) ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		return (Long) query.getSingleResult();
	}
	
	/**
	 * Total # of Developers with listings by certified body in each year
	 */
	@Override
	public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsEachYear(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT certificationBodyName, year, count(DISTINCT developerCode) FROM CertifiedProductDetailsEntity "
				+ " WHERE (deleted = false AND creationDate BETWEEN :creationStartDate AND :creationEndDate) "
				+ " OR (deleted = true AND creationDate BETWEEN :creationStartDate AND :creationEndDate AND lastModifiedDate > :creationEndDate) "
				+ " GROUP BY certificationBodyName, year "
				+ " ORDER BY certificationBodyName ");
		query.setParameter("creationStartDate", dateRange.getStartDate());
		query.setParameter("creationEndDate", dateRange.getEndDate());
		List<Object[]> results = query.getResultList();
		List<CertifiedBodyStatistics> cbStats = new ArrayList<CertifiedBodyStatistics>();
		for(Object[] obj : results){
			CertifiedBodyStatistics cbStat = new CertifiedBodyStatistics();
			cbStat.setName(obj[0].toString());
			cbStat.setYear(Integer.valueOf(obj[1].toString()));
			cbStat.setTotalDevelopersWithListings(Long.valueOf(obj[2].toString()));
			cbStat.setCertificationStatusName(null);
			cbStats.add(cbStat);
		}
		return cbStats;
	}
	
	/**
	 * Total # of Developers with listings by certified body in each year
	 */
	@Override
	public List<CertifiedBodyStatistics> getTotalDevelopersByCertifiedBodyWithListingsInEachCertificationStatusAndYear(DateRange dateRange) {
		Query query = entityManager.createQuery("SELECT certificationBodyName, year, count(DISTINCT developerCode), certificationStatusName FROM CertifiedProductDetailsEntity "
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
			stat.setTotalDevelopersWithListings(Long.valueOf(obj[2].toString()));
			stat.setCertificationStatusName(obj[3].toString());
			cbStats.add(stat);
		}
		return cbStats;
	}

}
