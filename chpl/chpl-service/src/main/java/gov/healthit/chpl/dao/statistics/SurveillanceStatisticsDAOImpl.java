package gov.healthit.chpl.dao.statistics;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;

@Repository("surveillanceStatisticsDAO")
public class SurveillanceStatisticsDAOImpl extends BaseDAOImpl implements SurveillanceStatisticsDAO{
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
}
