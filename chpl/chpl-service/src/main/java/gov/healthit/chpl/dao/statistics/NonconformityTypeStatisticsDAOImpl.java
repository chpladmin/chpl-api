package gov.healthit.chpl.dao.statistics;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.impl.BaseDAOImpl;
import gov.healthit.chpl.domain.DateRange;
import gov.healthit.chpl.dto.NonconformityTypeStatisticsDTO;
import gov.healthit.chpl.entity.surveillance.NonconformityTypeStatisticsEntity;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

@Repository("nonconformityTypeStatisticsDAO")
public class NonconformityTypeStatisticsDAOImpl extends BaseDAOImpl implements NonconformityTypeStatisticsDAO {

	@Override
	public List<NonconformityTypeStatisticsDTO> getAllNonconformityStatistics(
			DateRange dateRange) {
		String hql = "FROM NonconformityTypeStatisticsEntity WHERE";
		
		if(dateRange == null) {
            hql += " deleted = false";
		} else {
			hql += "(deleted = false AND creationDate <= :endDate) "
                + " OR "
                + "(deleted = true AND creationDate <= :endDate AND lastModifiedDate > :endDate)";
		}
		Query query = entityManager.createQuery(hql);
        
        if(dateRange != null) {
            query.setParameter("endDate", dateRange.getEndDate());
        }
        
        List<NonconformityTypeStatisticsEntity> entities = query.getResultList();
        
        List<NonconformityTypeStatisticsDTO> dtos = new ArrayList<NonconformityTypeStatisticsDTO>();
        for(NonconformityTypeStatisticsEntity entity : entities){
        	NonconformityTypeStatisticsDTO dto = new NonconformityTypeStatisticsDTO(entity);
        	dtos.add(dto);
        }
        
        return dtos;
	}
	
	public void create(NonconformityTypeStatisticsDTO dto){
		NonconformityTypeStatisticsEntity entity = new NonconformityTypeStatisticsEntity();
		entity.setNonconformityCount(dto.getNonconformityCount());
		entity.setNonconformityType(dto.getNonconformityType());
		if (dto.getLastModifiedDate() == null) {
            entity.setLastModifiedDate(new Date());
        } else {
            entity.setLastModifiedDate(dto.getLastModifiedDate());
        }
		
		if (dto.getLastModifiedUser() == null) {
            entity.setLastModifiedUser(-2L);
        } else {
            entity.setLastModifiedUser(dto.getLastModifiedUser());
        }
		
		if (dto.getDeleted() == null) {
            entity.setDeleted(false);
        } else {
            entity.setDeleted(dto.getDeleted());
        }
		entityManager.persist(entity);
		entityManager.flush();
	}
}
