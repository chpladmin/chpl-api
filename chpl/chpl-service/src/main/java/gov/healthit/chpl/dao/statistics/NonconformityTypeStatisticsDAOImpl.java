package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.domain.SurveillanceNonconformity;

import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository("nonconformityTypeStatisticsDAO")
public class NonconformityTypeStatisticsDAOImpl extends BaseDAOImpl implements NonconformityTypeStatisticsDAO {

	@Override
	public Long getAllNonconformities(
			DateRange dateRange) {
		String hql = "SELECT COUNT(DISTINCT id) FROM SurveillanceNonConformityEntity";
		
		if(dateRange == null) {
            hql += " deleted = false";
		} else {
			hql += "(deleted = false AND creationDate <= :endDate) "
                + " OR "
                + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
		}
		Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
	}

	@Override
	public Long getAllNonconformities2014Edition(
			DateRange dateRange) {
		String hql = "SELECT COUNT(DISTINCT id) FROM SurveillanceNonconformityEntity WHERE type LIKE '%170.314%'";
		
		if(dateRange == null) {
            hql += " deleted = false";
		} else {
			hql += "(deleted = false AND creationDate <= :endDate) "
                + " OR "
                + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
		}
		Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
	}

	@Override
	public Long getAllNonconformities2015Edition(
			DateRange dateRange) {
		String hql = "SELECT COUNT(DISTINCT id) FROM SurveillanceNonconformityEntity WHERE type LIKE '%170.315%'";
		
		if(dateRange == null) {
            hql += " deleted = false";
		} else {
			hql += "(deleted = false AND creationDate <= :endDate) "
                + " OR "
                + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
		}
		Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
	}

	@Override
	public Long getAllProgramNonconformities(
			DateRange dateRange) {
		String hql = "SELECT COUNT(DISTINCT id) FROM SurveillanceNonconformityEntity WHERE type LIKE '%170.523%'";
		
		if(dateRange == null) {
            hql += " deleted = false";
		} else {
			hql += "(deleted = false AND creationDate <= :endDate) "
                + " OR "
                + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) ";
		}
		Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
	}
	
}
