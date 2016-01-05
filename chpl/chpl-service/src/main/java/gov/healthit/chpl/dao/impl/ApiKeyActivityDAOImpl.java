package gov.healthit.chpl.dao.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Query;

import org.springframework.stereotype.Repository;

import gov.healthit.chpl.auth.Util;
import gov.healthit.chpl.dao.ApiKeyActivityDAO;
import gov.healthit.chpl.dao.ApiKeyDAO;
import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.entity.ApiKeyActivityEntity;

@Repository("apiKeyActivityDAO")
public class ApiKeyActivityDAOImpl extends BaseDAOImpl implements ApiKeyActivityDAO {

	@Override
	public ApiKeyActivityDTO create(ApiKeyActivityDTO dto) throws EntityCreationException {
		
		ApiKeyActivityEntity entity = null;
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
			
			entity = new ApiKeyActivityEntity();
			entity.setApiKeyId(dto.getApiKeyId());
			entity.setApiCallPath(dto.getApiCallPath());	
			
			if(dto.getLastModifiedDate() != null) {
				entity.setLastModifiedDate(dto.getLastModifiedDate());
			} else {
				entity.setLastModifiedDate(new Date());
			}
			if(dto.getCreationDate() != null) {
				entity.setCreationDate(dto.getCreationDate());
			} else {
				entity.setCreationDate(new Date());
			}
			entity.setDeleted(dto.getDeleted());
			
			if (Util.getCurrentUser() == null){
				entity.setLastModifiedUser(-3L);
			} else {
				entity.setLastModifiedUser(Util.getCurrentUser().getId());
			}
			
			create(entity);
		}
		return new ApiKeyActivityDTO(entity);
	}

	@Override
	public ApiKeyActivityDTO update(ApiKeyActivityDTO dto) throws EntityRetrievalException {
		
		ApiKeyActivityEntity entity = getEntityById(dto.getId());
		
		entity.setApiKeyId(dto.getApiKeyId());
		entity.setApiCallPath(dto.getApiCallPath());
		
		if(dto.getLastModifiedDate() != null) {
			entity.setLastModifiedDate(dto.getLastModifiedDate());
		} else {
			entity.setLastModifiedDate(new Date());
		}
		entity.setDeleted(dto.getDeleted());
		entity.setLastModifiedUser(Util.getCurrentUser().getId());
		update(entity);
		
		return new ApiKeyActivityDTO(entity);
	}

	@Override
	public void delete(Long id) {
		
		Query query = entityManager.createQuery("UPDATE ApiKeyActivityEntity SET deleted = true WHERE api_activity_id = :entityid");
		query.setParameter("entityid", id);
		query.executeUpdate();
		
	}

	@Override
	public List<ApiKeyActivityDTO> findAll() {
		
		List<ApiKeyActivityEntity> entities = getAllEntities();
		List<ApiKeyActivityDTO> dtos = new ArrayList<>();
		
		for (ApiKeyActivityEntity entity : entities) {
			ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}
	
	@Override
	public List<ApiKeyActivityDTO> findAll(Integer pageNumber, Integer pageSize) {
		
		List<ApiKeyActivityEntity> entities = getAllEntities(pageNumber, pageSize);
		List<ApiKeyActivityDTO> dtos = new ArrayList<>();
		
		for (ApiKeyActivityEntity entity : entities) {
			ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
		
	}
	

	@Override
	public ApiKeyActivityDTO getById(Long id) throws EntityRetrievalException {
		
		ApiKeyActivityDTO dto = null;
		ApiKeyActivityEntity entity = getEntityById(id);
		if (entity != null){
			dto = new ApiKeyActivityDTO(entity);
		}
		return dto;
		
	}

	@Override
	public List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId) {
		List<ApiKeyActivityEntity> entities = getActivityEntitiesByKeyId(apiKeyId);
		List<ApiKeyActivityDTO> dtos = new ArrayList<ApiKeyActivityDTO>();
		
		for (ApiKeyActivityEntity entity : entities){
			ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	@Override
	public List<ApiKeyActivityDTO> findByKeyId(Long apiKeyId, Integer pageNumber, Integer pageSize) {
		
		List<ApiKeyActivityEntity> entities = getActivityEntitiesByKeyId(apiKeyId, pageNumber, pageSize);
		List<ApiKeyActivityDTO> dtos = new ArrayList<ApiKeyActivityDTO>();
		
		for (ApiKeyActivityEntity entity : entities){
			ApiKeyActivityDTO dto = new ApiKeyActivityDTO(entity);
			dtos.add(dto);
		}
		return dtos;
	}
	
	
	private void create(ApiKeyActivityEntity entity) {
		
		entityManager.persist(entity);
		entityManager.flush();
	}
	
	private void update(ApiKeyActivityEntity entity) {
		
		entityManager.merge(entity);
		entityManager.flush();
	
	}
	
	private List<ApiKeyActivityEntity> getAllEntities() {
		
		List<ApiKeyActivityEntity> result = entityManager.createQuery( "from ApiKeyActivityEntity where (NOT deleted = true) ", ApiKeyActivityEntity.class).getResultList();
		return result;
	}
	
	private ApiKeyActivityEntity getEntityById(Long entityId) throws EntityRetrievalException {
		
		ApiKeyActivityEntity entity = null;
		
		Query query = entityManager.createQuery( "from ApiKeyActivityEntity where (NOT deleted = true) AND (api_activity_id = :entityid) ", ApiKeyActivityEntity.class );
		query.setParameter("entityid", entityId);
		List<ApiKeyActivityEntity> result = query.getResultList();
		
		if (result.size() > 1){
			throw new EntityRetrievalException("Data error. Duplicate api key id in database.");
		} else if(result.size() == 1) {
			entity = result.get(0);
		}
		return entity;
	}
	
	private List<ApiKeyActivityEntity> getAllEntities(Integer pageNumber, Integer pageSize) {
		
		Query query = entityManager.createQuery("from ApiKeyActivityEntity where (NOT deleted = true) ", ApiKeyActivityEntity.class);
		query.setMaxResults(pageSize);
	    query.setFirstResult(pageNumber * pageSize);
		List<ApiKeyActivityEntity> result = query.getResultList();
		
		return result;
	}
	
	private List<ApiKeyActivityEntity> getActivityEntitiesByKeyId(Long keyId) {
		
		Query query = entityManager.createQuery( "from ApiKeyActivityEntity where (NOT deleted = true) AND (api_key_id = :apikeyid) ", ApiKeyActivityEntity.class );
		query.setParameter("apikeyid", keyId);
		List<ApiKeyActivityEntity> result = query.getResultList();
		return result;
		
	}
	
	private List<ApiKeyActivityEntity> getActivityEntitiesByKeyId(Long keyId, Integer pageNumber, Integer pageSize) {
		
		
		Query query = entityManager.createQuery( "from ApiKeyActivityEntity where (NOT deleted = true) AND (api_key_id = :apikeyid) ", ApiKeyActivityEntity.class );
		query.setParameter("apikeyid", keyId);
		query.setMaxResults(pageSize);
	    query.setFirstResult(pageNumber * pageSize);
		
		List<ApiKeyActivityEntity> result = query.getResultList();
		return result;
		
	}
	
}
