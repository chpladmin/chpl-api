package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Repository;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.dao.CertificationStatusDAO;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.CertificationStatusDTO;
import gov.healthit.chpl.entity.CertificationStatusEntity;

@Repository("certificationStatusDAO")
public class CertificationStatusDAOImpl extends BaseDAOImpl implements CertificationStatusDAO {
	
	@Override
	public List<CertificationStatusDTO> findAll() {
		List<CertificationStatusEntity> entities = getAllEntities();
		List<CertificationStatusDTO> result = new ArrayList<CertificationStatusDTO>();
		for(CertificationStatusEntity entity : entities) {
			result.add(new CertificationStatusDTO(entity));
		}
		return result;
	}

	@Override
	public CertificationStatusDTO getById(Long id) throws EntityRetrievalException {
		
		CertificationStatusDTO dto = null;
		CertificationStatusEntity entity = getEntityById(id);
		if (entity != null){
			dto = new CertificationStatusDTO(entity);
		}
		return dto;
	}

	@Override
	@Cacheable(CacheNames.getByStatusName)
	public CertificationStatusDTO getByStatusName(String statusName) {
		CertificationStatusEntity entity = getEntityByName(statusName);
		return new CertificationStatusDTO(entity);
	}
	
	private List<CertificationStatusEntity> getAllEntities() {
		
		List<CertificationStatusEntity> result = entityManager.createQuery( "from CertificationStatusEntity where (NOT deleted = true) ", CertificationStatusEntity.class).getResultList();
		return result;
		
	}
	
	public CertificationStatusEntity getEntityById(Long id) throws EntityRetrievalException {
		
		CertificationStatusEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationStatusEntity where (NOT deleted = true) AND (certification_status_id = :entityid) ", CertificationStatusEntity.class );
		query.setParameter("entityid", id);
		List<CertificationStatusEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate status id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
	
	public CertificationStatusEntity getEntityByName(String name) {
		
		CertificationStatusEntity entity = null;
			
		Query query = entityManager.createQuery( "from CertificationStatusEntity where (NOT deleted = true) AND (certification_status = :name) ", CertificationStatusEntity.class );
		query.setParameter("name", name);
		List<CertificationStatusEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
			
		return entity;
	}
}
