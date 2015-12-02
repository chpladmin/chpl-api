package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.dto.ApiKeyDTO;

public interface ApiKeyManager {
	
	public ApiKeyDTO createKey(ApiKeyDTO toCreate) throws EntityCreationException;
	public void deleteKey(Long keyId);
	public void deleteKey(String keyString);
	public ApiKeyDTO findKey(Long keyId) throws EntityRetrievalException;
	public ApiKeyDTO findKey(String keyString);
	public List<ApiKeyDTO> findAll();
	public void logApiKeyActivity(String keyString, String activityPath) throws EntityCreationException;
	public List<ApiKeyActivity> getApiKeyActivity() throws EntityRetrievalException;
	public List<ApiKeyActivity> getApiKeyActivity(Integer pageNumber, Integer pageSize) throws EntityRetrievalException;
	public List<ApiKeyActivity> getApiKeyActivity(String keyString);
	public List<ApiKeyActivity> getApiKeyActivity(String keyString, Integer pageNumber, Integer pageSize);
	
	
}