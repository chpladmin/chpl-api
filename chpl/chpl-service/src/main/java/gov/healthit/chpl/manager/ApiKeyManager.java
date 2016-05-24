package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.dto.ApiKeyDTO;

public interface ApiKeyManager {
	
	public ApiKeyDTO createKey(ApiKeyDTO toCreate) throws EntityCreationException, JsonProcessingException, EntityRetrievalException;
	public void deleteKey(Long keyId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;
	public void deleteKey(String keyString) throws JsonProcessingException, EntityCreationException, EntityRetrievalException;
	public ApiKeyDTO findKey(Long keyId) throws EntityRetrievalException;
	public ApiKeyDTO findKey(String keyString);
	public List<ApiKeyDTO> findAll();
	public void logApiKeyActivity(String keyString, String activityPath) throws EntityCreationException;
	public List<ApiKeyActivity> getApiKeyActivity() throws EntityRetrievalException;
	public List<ApiKeyActivity> getApiKeyActivity(Integer pageNumber, Integer pageSize) throws EntityRetrievalException;
	public List<ApiKeyActivity> getApiKeyActivity(String keyString);
	public List<ApiKeyActivity> getApiKeyActivity(String keyString, Integer pageNumber, Integer pageSize);
	
	
}