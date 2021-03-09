package gov.healthit.chpl.api;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.api.dao.ApiKeyDAO;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.dao.ApiKeyActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;

@Service
public class ApiKeyManager {

    private ApiKeyDAO apiKeyDAO;
    private ApiKeyActivityDAO apiKeyActivityDAO;
    private ActivityManager activityManager;

    @Autowired
    public ApiKeyManager(ApiKeyDAO apiKeyDAO, ApiKeyActivityDAO apiKeyActivityDAO,
        ActivityManager activityManager) {
        this.apiKeyDAO = apiKeyDAO;
        this.apiKeyActivityDAO = apiKeyActivityDAO;
        this.activityManager = activityManager;
    }

    @Transactional
    public ApiKey createKey(final ApiKey toCreate)
            throws EntityCreationException, JsonProcessingException, EntityRetrievalException {

        ApiKey created = apiKeyDAO.create(toCreate);

        String activityMsg = "API Key " + created.getApiKey() + " was created.";
        activityManager.addActivity(ActivityConcept.API_KEY, created.getId(), activityMsg, null,
                created);
        return created;

    }

    @Transactional
    public ApiKey updateApiKey(final ApiKey dto) throws EntityRetrievalException {
        return apiKeyDAO.update(dto);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public void deleteKey(final Long keyId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {

        ApiKey toDelete = apiKeyDAO.getById(keyId);

        String activityMsg = "API Key " + toDelete.getApiKey() + " was revoked.";

        apiKeyDAO.delete(keyId);
        activityManager.addActivity(ActivityConcept.API_KEY, toDelete.getId(), activityMsg, toDelete,
                null);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public void deleteKey(final String keyString)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException {

        ApiKey toDelete = apiKeyDAO.getByKey(keyString);

        String activityMsg = "API Key " + toDelete.getApiKey() + " was revoked.";

        apiKeyDAO.delete(toDelete.getId());
        activityManager.addActivity(ActivityConcept.API_KEY, toDelete.getId(), activityMsg, toDelete,
                null);
    }

    @Transactional
    public ApiKey findKey(final Long keyId) throws EntityRetrievalException {
        return apiKeyDAO.getById(keyId);
    }

    @Transactional
    public ApiKey findKey(final String keyString) throws EntityRetrievalException {
        return apiKeyDAO.getByKey(keyString);
    }

    @Transactional
    public void logApiKeyActivity(final String keyString, final String apiCallPath, final String apiCallMethod)
            throws EntityRetrievalException, EntityCreationException {

        ApiKey apiKey = findKey(keyString);
        ApiKeyActivityDTO apiKeyActivityDto = new ApiKeyActivityDTO();

        apiKeyActivityDto.setApiCallPath(apiCallPath);
        apiKeyActivityDto.setApiCallMethod(apiCallMethod);
        apiKeyActivityDto.setApiKeyId(apiKey.getId());
        apiKeyActivityDto.setDeleted(false);

        apiKeyActivityDAO.create(apiKeyActivityDto);

        // Update the lastUsedDate...
        apiKey.setLastUsedDate(new Date());
        apiKeyDAO.update(apiKey);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ApiKey> findAll(final Boolean includeDeleted) {
        return apiKeyDAO.findAll(includeDeleted);
    }
}
