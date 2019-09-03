package gov.healthit.chpl.manager;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;

public interface ApiKeyManager {

    ApiKeyDTO createKey(ApiKeyDTO toCreate)
            throws EntityCreationException, JsonProcessingException, EntityRetrievalException;

    void deleteKey(Long keyId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException;

    void deleteKey(String keyString)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException;

    ApiKeyDTO findKey(Long keyId) throws EntityRetrievalException;

    ApiKeyDTO findKey(String keyString) throws EntityRetrievalException;

    List<ApiKeyDTO> findAll(Boolean includeDeleted);

    void logApiKeyActivity(String keyString, String activityPath)
            throws EntityRetrievalException, EntityCreationException;

    List<ApiKeyActivity> getApiKeyActivity() throws EntityRetrievalException;

    List<ApiKeyActivity> getApiKeyActivity(String apiKeyFilter, Integer pageNumber, Integer pageSize,
            boolean dateAscending, Long startDate, Long endDate) throws EntityRetrievalException;

    List<ApiKeyActivity> getApiKeyActivity(String keyString) throws EntityRetrievalException;

    List<ApiKeyActivity> getApiKeyActivity(String keyString, Integer pageNumber, Integer pageSize)
            throws EntityRetrievalException;

    ApiKeyDTO updateApiKey(ApiKeyDTO dto) throws EntityRetrievalException;
}
