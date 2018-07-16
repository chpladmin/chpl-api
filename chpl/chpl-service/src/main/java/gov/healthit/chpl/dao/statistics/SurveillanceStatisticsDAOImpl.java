package gov.healthit.chpl.dao.statistics;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;

@Repository("surveillanceStatisticsDAO")
public class SurveillanceStatisticsDAOImpl extends BaseDAOImpl implements SurveillanceStatisticsDAO {
    /**
     * Total # of Surveillance Activities*
     */
    @Override
    public Long getTotalSurveillanceActivities(DateRange dateRange) {
        String hql = "SELECT count(*) "
                + "FROM SurveillanceEntity "
                + "WHERE ";
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

    /**
     * Open Surveillance Activities
     */
    @Override
    public Long getTotalOpenSurveillanceActivities(DateRange dateRange) {
       String hql = "SELECT count(*) "
                + "FROM SurveillanceEntity "
                + "WHERE startDate <= now() "
                + "AND (endDate IS NULL OR endDate >= now()) ";
       if(dateRange == null) {
           hql += " AND deleted = false";
       } else {
           hql += "AND ((deleted = false AND creationDate <= :endDate) "
                   + " OR "
                   + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
       }

       Query query = entityManager.createQuery(hql);
       if(dateRange != null) {
           query.setParameter("endDate", dateRange.getEndDate());
       }
        return (Long) query.getSingleResult();
    }

    /**
     * Closed Surveillance Activities
     */
    @Override
    public Long getTotalClosedSurveillanceActivities(DateRange dateRange) {
        String hql = "SELECT count(*) "
                + "FROM SurveillanceEntity "
                + "WHERE startDate <= now() "
                + "AND (endDate IS NOT NULL AND endDate <= now()) ";
        if(dateRange == null) {
            hql += " AND deleted = false";
        } else {
            hql += "AND ((deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Total # of NCs
     */
    @Override
    public Long getTotalNonConformities(DateRange dateRange) {
        String hql = "SELECT count(*) "
                + "FROM SurveillanceNonconformityEntity "
                + "WHERE ";
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

    /**
     * Open NCs
     */
    @Override
    public Long getTotalOpenNonconformities(DateRange dateRange) {
        String hql = "SELECT count(*) "
                + "FROM SurveillanceNonconformityEntity "
                + "WHERE nonconformityStatusId = 1 ";
        if(dateRange == null) {
            hql += " AND deleted = false";
        } else {
            hql += " AND ((deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }

    /**
     * Closed NCs
     */
    @Override
    public Long getTotalClosedNonconformities(DateRange dateRange) {
        String hql = "SELECT count(*) "
                + "FROM SurveillanceNonconformityEntity "
                + "WHERE nonconformityStatusId = 2 ";
        if(dateRange == null) {
            hql += " AND deleted = false";
        } else {
            hql += " AND ((deleted = false AND creationDate <= :endDate) "
                    + " OR "
                    + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)) ";
        }

        Query query = entityManager.createQuery(hql);
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        return (Long) query.getSingleResult();
    }
    
    @Override
	public List<NonconformityTypeStatisticsDTO> getAllNonconformitiesByCriterion(
			DateRange dateRange) {
		String hql = "SELECT COUNT(type), type FROM SurveillanceNonconformityEntity WHERE";
		
		if(dateRange == null) {
            hql += " deleted = false GROUP BY type";
		} else {
			hql += "(deleted = false AND creationDate <= :endDate) "
                + " OR "
                + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate) GROUP BY type";
		}
		Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        
        List<Object[]> entities = query.getResultList();
        
        List<NonconformityTypeStatisticsDTO> dtos =  new ArrayList<NonconformityTypeStatisticsDTO>();
        for(Object[] entity : entities){
        	NonconformityTypeStatisticsDTO dto = new NonconformityTypeStatisticsDTO();
        	dto.setNonconformityCount(Long.valueOf(entity[0].toString()));
        	dto.setNonconformityType(entity[1].toString());
        	dtos.add(dto);
        }
        
        return dtos;
	}
    
}
