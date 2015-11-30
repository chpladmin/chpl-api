package gov.healthit.chpl.manager;

import java.util.List;

import gov.healthit.chpl.dao.EntityCreationException;
import gov.healthit.chpl.dao.EntityRetrievalException;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.dto.ApiKeyDTO;

public interface ApiKeyManager {
	
	public ApiKeyDTO createKey(ApiKeyDTO toCreate) throws EntityCreationException;
	public void deleteKey(Long keyId);
	public ApiKeyDTO findKey(Long keyId) throws EntityRetrievalException;
	public ApiKeyDTO findKey(String keyString);
	public void logApiKeyActivity(String keyString, String activityPath) throws EntityCreationException;
	public List<ApiKeyActivity> getApiKeyActivity(String keyString);
	
}