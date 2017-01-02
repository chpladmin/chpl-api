package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dao.PracticeTypeDAO;
import gov.healthit.chpl.dto.PracticeTypeDTO;
import gov.healthit.chpl.entity.PracticeTypeEntity;

@Repository("practiceTypeDAO")
public class PracticeTypeDAOImpl extends BaseDAOImpl implements PracticeTypeDAO {

	@Override
	public void create(PracticeTypeDTO dto) throws EntityCreationException,
			EntityRetrievalException {
		
		PracticeTypeEntity entity = null;
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
			
			entity = new PracticeTypeEntity();
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setId(dto.getId());
			entity.setName(dto.getName());
			entity.setDescription(dto.getDescription());
			//entity.setLastModifiedDate(result.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
			create(entity);	
		}
		
	}

	@Override
	public void update(PracticeTypeDTO dto) throws EntityRetrievalException {
		
		PracticeTypeEntity entity = this.getEntityById(dto.getId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setName(dto.getName());
		entity.setDescription(dto.getDescription());
		//entity.setLastModifiedDate(result.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
			
		update(entity);
	}

	@Override
	public void delete(Long id) {
		Query query = entityManager.createQuery("UPDATE PracticeTypeEntity SET deleted = true WHERE practice_type_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
	}

	@Override
	public List<PracticeTypeDTO> findAll() {
		
		List<PracticeTypeEntity> entities = getAllEntities();
		List<PracticeTypeDTO> dtos = new ArrayList<>();
		
		for (PracticeTypeEntity entity : entities) {
			PracticeTypeDTO dto = new PracticeTypeDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}

	@Override
	public PracticeTypeDTO getById(Long id) throws EntityRetrievalException {
		
		PracticeTypeDTO dto = null;
		PracticeTypeEntity entity = getEntityById(id);
		if (entity != null){
			dto = new PracticeTypeDTO(entity);
		}
		return dto;	
	}
	
	@Override
	public PracticeTypeDTO getByName(String name) {
		
		PracticeTypeEntity entity = getEntityByName(name);
		PracticeTypeDTO dto = new PracticeTypeDTO(entity);
		return dto;
		
	}
	
	private void create(PracticeTypeEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(PracticeTypeEntity entity) {
		
		entityManager.merge(entity);	
		entityManager.flush();
	}
	
	private List<PracticeTypeEntity> getAllEntities() {
		
		List<PracticeTypeEntity> result = entityManager.createQuery( "from PracticeTypeEntity where (NOT deleted = true) ", PracticeTypeEntity.class).getResultList();
		return result;
		
	}
	
	private PracticeTypeEntity getEntityById(Long id) throws EntityRetrievalException {
		
		PracticeTypeEntity entity = null;
			
		Query query = entityManager.createQuery( "from PracticeTypeEntity where (NOT deleted = true) AND (practice_type_id = :entityid) ", PracticeTypeEntity.class );
		query.setParameter("entityid", id);
		List<PracticeTypeEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate developer id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		
		return entity;
	}
	
	private PracticeTypeEntity getEntityByName(String name) {			
		Query query = entityManager.createQuery( "from PracticeTypeEntity where (NOT deleted = true) AND (name = :name) ", PracticeTypeEntity.class );
		query.setParameter("name", name);
		List<PracticeTypeEntity> result = query.getResultList();
		
		if(result.size() == 0) {
			return null;
		}
		
		return result.get(0);
	}
}
