package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.UcdProcessDAO;
import gov.healthit.chpl.dto.UcdProcessDTO;
import gov.healthit.chpl.entity.UcdProcessEntity;

@Repository("ucdProcessDAO")
public class UcdProcessDAOImpl extends BaseDAOImpl implements UcdProcessDAO {
	
	@Override
	public UcdProcessDTO create(UcdProcessDTO dto)
			throws EntityCreationException, EntityRetrievalException {
		
		UcdProcessEntity entity = null;
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
			entity = new UcdProcessEntity();
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setName(dto.getName());
			
			create(entity);
			return new UcdProcessDTO(entity);
		}		
	}

	@Override
	public UcdProcessDTO update(UcdProcessDTO dto)
			throws EntityRetrievalException {
		UcdProcessEntity entity = this.getEntityById(dto.getId());
		
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}
		
		entity.setName(dto.getName());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setLastModifiedDate(new Date());
		
		update(entity);
		return new UcdProcessDTO(entity);
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		
		UcdProcessEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public UcdProcessDTO getById(Long id)
			throws EntityRetrievalException {
		
		UcdProcessDTO dto = null;
		UcdProcessEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new UcdProcessDTO(entity);
		}
		return dto;
	}
	
	@Override
	public UcdProcessDTO getByName(String name ) {
		
		UcdProcessDTO dto = null;
		List<UcdProcessEntity> entities = getEntitiesByName(name);
		
		if (entities != null && entities.size() > 0){
			dto = new UcdProcessDTO(entities.get(0));
		}
		return dto;
	}
	
	@Override
	public List<UcdProcessDTO> findAll() {
		
		List<UcdProcessEntity> entities = getAllEntities();
		List<UcdProcessDTO> dtos = new ArrayList<UcdProcessDTO>();
		
		for (UcdProcessEntity entity : entities) {
			UcdProcessDTO dto = new UcdProcessDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private void create(UcdProcessEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(UcdProcessEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<UcdProcessEntity> getAllEntities() {
		return entityManager.createQuery( "from UcdProcessEntity where (NOT deleted = true) ", UcdProcessEntity.class).getResultList();
	}
	
	private UcdProcessEntity getEntityById(Long id) throws EntityRetrievalException {
		
		UcdProcessEntity entity = null;
			
		Query query = entityManager.createQuery( "from UcdProcessEntity where (NOT deleted = true) AND (ucd_process_id = :entityid) ", UcdProcessEntity.class );
		query.setParameter("entityid", id);
		List<UcdProcessEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate test tool id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private List<UcdProcessEntity> getEntitiesByName(String name) {
		
		Query query = entityManager.createQuery( "from UcdProcessEntity where (NOT deleted = true) AND (name = :name) ", UcdProcessEntity.class );
		query.setParameter("name", name);
		List<UcdProcessEntity> result = query.getResultList();
		
		return result;
	}
}