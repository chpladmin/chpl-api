package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Query;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ApiKeyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.entity.ApiKeyEntity;


public class ApiKeyDAOImpl extends BaseDAOImpl implements ApiKeyDAO {

	@Override
	public ApiKeyDTO create(ApiKeyDTO dto) throws EntityCreationException {
		
		ApiKeyEntity entity = null;
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
			
			entity = new ApiKeyEntity();
			entity.setApiKey(dto.getApiKey());
			entity.setEmail(dto.getEmail());
			entity.setNameOrganization(dto.getNameOrganization());
			entity.setCreationDate(dto.getCreationDate());
			entity.setDeleted(dto.getDeleted());
			entity.setLastModifiedUser(Util.getCurrentUser().getId());
			create(entity);
		}
		return new ApiKeyDTO(entity);
	}

	@Override
	public ApiKeyDTO update(ApiKeyDTO dto) throws EntityRetrievalException {
		
		ApiKeyEntity entity = getEntityById(dto.getId());
		
		entity.setApiKey(dto.getApiKey());
		entity.setEmail(dto.getEmail());
		entity.setNameOrganization(dto.getNameOrganization());
		entity.setCreationDate(dto.getCreationDate());
		entity.setDeleted(dto.getDeleted());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		update(entity);
		
		return new ApiKeyDTO(entity);
	}

	@Override
	public void delete(Long id) {
		
		Query query = entityManager.createQuery("UPDATE ApiKeyEntity SET deleted = true WHERE api_key_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
		
	}

	@Override
	public List<ApiKeyDTO> findAll() {
		
		List<ApiKeyEntity> entities = getAllEntities();
		List<ApiKeyDTO> dtos = new ArrayList<>();
		
		for (ApiKeyEntity entity : entities) {
			ApiKeyDTO dto = new ApiKeyDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}

	@Override
	public ApiKeyDTO getById(Long id) throws EntityRetrievalException {
		
		ApiKeyDTO dto = null;
		ApiKeyEntity entity = getEntityById(id);
		if (entity != null){
			dto = new ApiKeyDTO(entity);
		}
		return dto;
		
	}

	@Override
	public ApiKeyDTO getByKey(String apiKey) {
		ApiKeyEntity entity = getEntityByKey(apiKey);
		ApiKeyDTO dto = new ApiKeyDTO(entity);
		return dto;
	}
	
	
	private void create(ApiKeyEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(ApiKeyEntity entity) {
		
		entityManager.merge(entity);
		entityManager.flush();
	
	}
	
	private List<ApiKeyEntity> getAllEntities() {
		
		List<ApiKeyEntity> result = entityManager.createQuery( "from ApiKeyEntity where (NOT deleted = true) ", ApiKeyEntity.class).getResultList();
		return result;
	}
	
	private ApiKeyEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		ApiKeyEntity entity = null;
		
		Query query = entityManager.createQuery( "from ApiKeyEntity where (NOT deleted = true) AND (api_key_id = :entityid) ", ApiKeyEntity.class );
		query.setParameter("entityid", entityId);
		List<ApiKeyEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate api key id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}

		return entity;
	}
	
	private ApiKeyEntity getEntityByKey(String key) {
		
		ApiKeyEntity entity = null;
		
		
		Query query = entityManager.createQuery( "from ApiKeyEntity where (NOT deleted = true) AND (api_key = :apikey) ", ApiKeyEntity.class );
		query.setParameter("apikey", key);
		List<ApiKeyEntity> result = query.getResultList();
		
		if(result != null && result.size() > 0) {
			entity = result.get(0);
		}
		return entity;
	}
	

}
