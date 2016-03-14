package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.TargetedUserDAO;
import gov.healthit.chpl.dto.TargetedUserDTO;
import gov.healthit.chpl.entity.TargetedUserEntity;

@Repository("targetedUserDao")
public class TargetedUserDAOImpl extends BaseDAOImpl implements TargetedUserDAO {
	
	@Override
	public TargetedUserDTO create(TargetedUserDTO dto)
			throws EntityCreationException, EntityRetrievalException {
		
		TargetedUserEntity entity = null;
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
			entity = new TargetedUserEntity();
			entity.setCreationDate(new Date());
			entity.setDeleted(false);
			entity.setLastModifiedDate(new Date());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setName(dto.getName());
			create(entity);
			return new TargetedUserDTO(entity);
		}		
	}

	@Override
	public TargetedUserDTO update(TargetedUserDTO dto)
			throws EntityRetrievalException {
		TargetedUserEntity entity = this.getEntityById(dto.getId());
		
		if(entity == null) {
			throw new EntityRetrievalException("Entity with id " + dto.getId() + " does not exist");
		}
		
		entity.setName(dto.getName());
		
		update(entity);
		return new TargetedUserDTO(entity);
	}

	@Override
	public void delete(Long id) throws EntityRetrievalException {
		
		TargetedUserEntity toDelete = getEntityById(id);
		
		if(toDelete != null) {
			toDelete.setDeleted(true);
			toDelete.setLastModifiedDate(new Date());
			toDelete.setLastModifiedUser(Util.getCurrentUser().getId());
			update(toDelete);
		}
	}

	@Override
	public TargetedUserDTO getById(Long id)
			throws EntityRetrievalException {
		
		TargetedUserDTO dto = null;
		TargetedUserEntity entity = getEntityById(id);
		
		if (entity != null){
			dto = new TargetedUserDTO(entity);
		}
		return dto;
	}
	
	@Override
	public TargetedUserDTO getByName(String name) {
		
		TargetedUserDTO dto = null;
		List<TargetedUserEntity> entities = getEntitiesByName(name);
		
		if (entities != null && entities.size() > 0){
			dto = new TargetedUserDTO(entities.get(0));
		}
		return dto;
	}
	
	@Override
	public List<TargetedUserDTO> findAll() {
		
		List<TargetedUserEntity> entities = getAllEntities();
		List<TargetedUserDTO> dtos = new ArrayList<TargetedUserDTO>();
		
		for (TargetedUserEntity entity : entities) {
			TargetedUserDTO dto = new TargetedUserDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	private void create(TargetedUserEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
		
	}
	
	private void update(TargetedUserEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<TargetedUserEntity> getAllEntities() {
		return entityManager.createQuery( "from TargetedUserEntity where (NOT deleted = true) ", TargetedUserEntity.class).getResultList();
	}
	
	private TargetedUserEntity getEntityById(Long id) throws EntityRetrievalException {
		
		TargetedUserEntity entity = null;
			
		Query query = entityManager.createQuery( "from TargetedUserEntity where (NOT deleted = true) AND (id = :entityid) ", TargetedUserEntity.class );
		query.setParameter("entityid", id);
		List<TargetedUserEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate targeted user id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	
	private List<TargetedUserEntity> getEntitiesByName(String name) {
		
		Query query = entityManager.createQuery( "from TargetedUserEntity where (NOT deleted = true) AND (name = :name) ", TargetedUserEntity.class );
		query.setParameter("name", name);
		List<TargetedUserEntity> result = query.getResultList();
		
		return result;
	}
	
	
}