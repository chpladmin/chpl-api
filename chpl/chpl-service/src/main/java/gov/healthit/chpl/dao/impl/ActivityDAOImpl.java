package gov.healthit.chpl.dao.impl;

import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ActivityDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ActivityDTO;
import gov.healthit.chpl.entity.ActivityEntity;


public class ActivityDAOImpl extends BaseDAOImpl implements ActivityDAO {

	@Override
	public ActivityDTO create(ActivityDTO dto) throws EntityCreationException,
			EntityRetrievalException {
		
		ActivityEntity entity = null;
		try {
			if (dto.getId() != null){
				entity = this.getEntityById(dto.getId());
			}
		} catch (EntityRetrievalException e) {
			throw new EntityCreationException(e);
		}
		
		if (entity != null) {
			throw new EntityCreationException("An entity with this ID already exists.");
		} else {
			
			entity = new ActivityEntity();
			
			entity.setId(dto.getId());
			entity.setDescription(dto.getDescription());
			entity.setActivityDate(dto.getActivityDate());
			entity.setActivityObjectClassId(dto.getActivityObjectClassId());
			entity.setActivityObjectId(dto.getActivityObjectId());
			entity.setCreationDate(new Date());
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setDeleted(dto.getDeleted());
			
			create(entity);
			
		}
		ActivityDTO result = null;
		if (entity != null){
			result = new ActivityDTO(entity);
		}
		return result;
	}

	@Override
	public ActivityDTO update(ActivityDTO dto) throws EntityRetrievalException {
		
		ActivityEntity entity =  this.getEntityById(dto.getId());
		
		entity.setId(dto.getId());
		entity.setDescription(dto.getDescription());
		entity.setActivityDate(dto.getActivityDate());
		entity.setActivityObjectClassId(dto.getActivityObjectClassId());
		entity.setActivityObjectId(dto.getActivityObjectId());
		entity.setCreationDate(new Date());
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setDeleted(dto.getDeleted());
		
		update(entity);
		
		ActivityDTO result = null;
		if (entity != null){
			result = new ActivityDTO(entity);
		}
		return result;
		
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		// TODO Auto-generated method stub

	}

	@Override
	public ActivityDTO getById(Long id) throws EntityRetrievalException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ActivityDTO> findAll() {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	private void create(ActivityEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(ActivityEntity entity) {
		
		entityManager.merge(entity);
		entityManager.flush();
	
	}
	
	
	private ActivityEntity getEntityById(Long id) throws EntityRetrievalException {
		
		ActivityEntity entity = null;
			
		Query query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) AND (additional_software_id = :entityid) ", ActivityEntity.class );
		query.setParameter("entityid", id);
		List<ActivityEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private ActivityEntity getEntityByName(String name) {
		ActivityEntity entity = null;
			
		Query query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) AND (name = :name) ", ActivityEntity.class );
		query.setParameter("name", name);
		List<ActivityEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}

	private List<ActivityEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {
		
		Query query = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", ActivityEntity.class );
		query.setParameter("entityid", certifiedProductId);
		List<ActivityEntity> result = query.getResultList();
		return result;
	}
	
	private List<ActivityEntity> getAllEntities() {
		
		List<ActivityEntity> result = entityManager.createQuery( "from ActivityEntity where (NOT deleted = true) ", ActivityEntity.class).getResultList();
		return result;
	}

}
