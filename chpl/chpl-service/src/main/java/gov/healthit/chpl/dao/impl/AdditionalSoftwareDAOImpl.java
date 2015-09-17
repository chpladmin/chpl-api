package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.AdditionalSoftwareDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.AdditionalSoftwareDTO;
import gov.healthit.chpl.entity.AdditionalSoftwareEntity;

@Repository("additionalSoftwareDAO")
public class AdditionalSoftwareDAOImpl extends BaseDAOImpl implements AdditionalSoftwareDAO {

	@Override
	public void create(AdditionalSoftwareDTO dto)
			throws EntityCreationException {
		
		AdditionalSoftwareEntity entity = null;
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
			
			entity = new AdditionalSoftwareEntity();
			
			entity.setCertifiedProductId(dto.getCertifiedProductId());
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setId(dto.getId());
			entity.setJustification(dto.getJustification());
			//entity.setLastModifiedDate(dto.getLastModifiedDate());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			entity.setName(dto.getName());
			entity.setVersion(dto.getVersion());
			
			create(entity);
			
		}
	}

	@Override
	public void delete(Long id) {
		// TODO: How to delete this without leaving orphans
		Query query = entityManager.createQuery("UPDATE AdditionalSoftwareEntity SET deleted = true WHERE certification_result_id = :resultid");
		query.setParameter("resultid", id);
		query.executeUpdate();
	}

	@Override
	public List<AdditionalSoftwareDTO> findAll() {
		
		List<AdditionalSoftwareEntity> entities = getAllEntities();
		List<AdditionalSoftwareDTO> additionalSoftwares = new ArrayList<>();
		
		for (AdditionalSoftwareEntity entity : entities) {
			AdditionalSoftwareDTO result = new AdditionalSoftwareDTO(entity);
			additionalSoftwares.add(result);
		}
		return additionalSoftwares;
		
	}

	@Override
	public List<AdditionalSoftwareDTO> findByCertifiedProductId(Long id) {
		
		List<AdditionalSoftwareEntity> entities = getEntitiesByCertifiedProductId(id);
		List<AdditionalSoftwareDTO> cqmResults = new ArrayList<>();
		
		for (AdditionalSoftwareEntity entity : entities) {
			AdditionalSoftwareDTO cqmResult = new AdditionalSoftwareDTO(entity);
			cqmResults.add(cqmResult);
		}
		return cqmResults;
	}

	@Override
	public AdditionalSoftwareDTO getById(Long id)
			throws EntityRetrievalException {
		
		AdditionalSoftwareEntity entity = getEntityById(id);
		AdditionalSoftwareDTO dto = new AdditionalSoftwareDTO(entity);
		return dto;
		
	}

	@Override
	public AdditionalSoftwareDTO getByName(String name) {
		AdditionalSoftwareEntity entity = getEntityByName(name);
		if(entity == null || entity.getId() == null) {
			return null;
		}
		AdditionalSoftwareDTO dto = new AdditionalSoftwareDTO(entity);
		return dto;
	}
	
	@Override
	public void update(AdditionalSoftwareDTO dto)
			throws EntityRetrievalException {
		
		AdditionalSoftwareEntity entity = new AdditionalSoftwareEntity();
		
		entity.setCertifiedProductId(dto.getCertifiedProductId());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setId(dto.getId());
		entity.setJustification(dto.getJustification());
		//entity.setLastModifiedDate(dto.getLastModifiedDate());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		entity.setName(dto.getName());
		entity.setVersion(dto.getVersion());
		
		create(entity);
		
	}
	
	
	private void create(AdditionalSoftwareEntity entity) {
		
		entityManager.persist(entity);
		
	}
	
	private void update(AdditionalSoftwareEntity entity) {
		
		entityManager.merge(entity);	
	
	}
	
	
	private AdditionalSoftwareEntity getEntityById(Long id) throws EntityRetrievalException {
		
		AdditionalSoftwareEntity entity = null;
			
		Query query = entityManager.createQuery( "from AdditionalSoftwareEntity where (NOT deleted = true) AND (certification_criterion_id = :entityid) ", AdditionalSoftwareEntity.class );
		query.setParameter("entityid", id);
		List<AdditionalSoftwareEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate criterion id in database.");
		}
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}
	
	private AdditionalSoftwareEntity getEntityByName(String name) {
		AdditionalSoftwareEntity entity = null;
			
		Query query = entityManager.createQuery( "from AdditionalSoftwareEntity where (NOT deleted = true) AND (name = :name) ", AdditionalSoftwareEntity.class );
		query.setParameter("name", name);
		List<AdditionalSoftwareEntity> result = query.getResultList();
		
		if (result.size() > 0){
			entity = result.get(0);
		}
		return entity;
	}

	private List<AdditionalSoftwareEntity> getEntitiesByCertifiedProductId(Long certifiedProductId) {
		
		Query query = entityManager.createQuery( "from AdditionalSoftwareEntity where (NOT deleted = true) AND (certified_product_id = :entityid) ", AdditionalSoftwareEntity.class );
		query.setParameter("entityid", certifiedProductId);
		List<AdditionalSoftwareEntity> result = query.getResultList();
		return result;
	}
	
	private List<AdditionalSoftwareEntity> getAllEntities() {
		
		List<AdditionalSoftwareEntity> result = entityManager.createQuery( "from AdditionalSoftwareEntity where (NOT deleted = true) ", AdditionalSoftwareEntity.class).getResultList();
		return result;
	}

}
