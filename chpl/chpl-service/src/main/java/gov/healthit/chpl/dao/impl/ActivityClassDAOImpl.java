package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ActivityClassDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ActivityClassDTO;
import gov.healthit.chpl.entity.ActivityClassEntity;

@Repository("activityClassDAO")
public class ActivityClassDAOImpl extends BaseDAOImpl implements ActivityClassDAO {

	@Override
	public ActivityClassDTO create(ActivityClassDTO dto) throws EntityCreationException,
			EntityRetrievalException {
		
		ActivityClassEntity entity = null;
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
			
			entity = new ActivityClassEntity();
			
			entity.setId(dto.getId());
			entity.setClassName(dto.getClassName());
			entity.setCreationDate(new Date());
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setDeleted(dto.getDeleted());
			
			create(entity);
			
		}
		ActivityClassDTO result = null;
		if (entity != null){
			result = new ActivityClassDTO(entity);
		}
		return result;
	}

	@Override
	public ActivityClassDTO update(ActivityClassDTO dto) throws EntityRetrievalException {
		
		ActivityClassEntity entity =  this.getEntityById(dto.getId());
		
		entity.setId(dto.getId());
		entity.setClassName(dto.getClassName());
		entity.setCreationDate(new Date());
		entity.setLastModifiedDate(new Date());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setDeleted(dto.getDeleted());
		
		update(entity);
		
		ActivityClassDTO result = null;
		if (entity != null){
			result = new ActivityClassDTO(entity);
		}
		return result;
		
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		
		Query query = entityManager.createQuery("UPDATE ActivityClassEntity SET deleted = true WHERE ActivityClass_id = :resultid");
		query.setParameter("resultid", id);
		query.executeUpdate();

	}

	@Override
	public ActivityClassDTO getById(Long id) throws EntityRetrievalException {
		
		ActivityClassEntity entity = getEntityById(id);
		ActivityClassDTO dto = null;
		if (entity != null){
			dto = new ActivityClassDTO(entity);
		}
		return dto;
	}

	@Override
	public List<ActivityClassDTO> findAll() {
		
		List<ActivityClassEntity> entities = getAllEntities();
		List<ActivityClassDTO> activities = new ArrayList<>();
		
		for (ActivityClassEntity entity : entities) {
			ActivityClassDTO result = new ActivityClassDTO(entity);
			activities.add(result);
		}
		return activities;
	}
	
	
	
	private void create(ActivityClassEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(ActivityClassEntity entity) {
		
		entityManager.merge(entity);
		entityManager.flush();
	
	}
	
	
	private ActivityClassEntity getEntityById(Long id) throws EntityRetrievalException {
		
		ActivityClassEntity entity = null;
			
		Query query = entityManager.createQuery( "from ActivityClassEntity where (NOT deleted = true) AND (additional_software_id = :entityid) ", ActivityClassEntity.class );
		query.setParameter("entityid", id);
		List<ActivityClassEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private ActivityClassEntity getEntityByName(String name) {
		ActivityClassEntity entity = null;
			
		Query query = entityManager.createQuery( "from ActivityClassEntity where (NOT deleted = true) AND (name = :name) ", ActivityClassEntity.class );
		query.setParameter("name", name);
		List<ActivityClassEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}

	private List<ActivityClassEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {
		
		Query query = entityManager.createQuery( "from ActivityClassEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", ActivityClassEntity.class );
		query.setParameter("entityid", certifiedProductId);
		List<ActivityClassEntity> result = query.getResultList();
		return result;
	}
	
	private List<ActivityClassEntity> getAllEntities() {
		
		List<ActivityClassEntity> result = entityManager.createQuery( "from ActivityClassEntity where (NOT deleted = true) ", ActivityClassEntity.class).getResultList();
		return result;
	}

	
}
