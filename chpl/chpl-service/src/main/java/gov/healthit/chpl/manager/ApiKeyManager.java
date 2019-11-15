package gov.healthit.chpl.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.dao.ApiKeyActivityDAO;
import gov.healthit.chpl.dao.ApiKeyDAO;
import gov.healthit.chpl.domain.ApiKeyActivity;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.dto.ApiKeyDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.manager.ApiKeyManager;

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
    public ApiKeyDTO createKey(final ApiKeyDTO toCreate)
            throws EntityCreationException, JsonProcessingException, EntityRetrievalException {

        ApiKeyDTO created = apiKeyDAO.create(toCreate);

        String activityMsg = "API Key " + created.getApiKey() + " was created.";
        activityManager.addActivity(ActivityConcept.API_KEY, created.getId(), activityMsg, null,
                created);
        return created;

    }

    @Transactional
    public ApiKeyDTO updateApiKey(final ApiKeyDTO dto) throws EntityRetrievalException {
        return apiKeyDAO.update(dto);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public void deleteKey(final Long keyId)
            throws EntityRetrievalException, JsonProcessingException, EntityCreationException {

        ApiKeyDTO toDelete = apiKeyDAO.getById(keyId);

        String activityMsg = "API Key " + toDelete.getApiKey() + " was revoked.";

        apiKeyDAO.delete(keyId);
        activityManager.addActivity(ActivityConcept.API_KEY, toDelete.getId(), activityMsg, toDelete,
                null);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public void deleteKey(final String keyString)
            throws JsonProcessingException, EntityCreationException, EntityRetrievalException {

        ApiKeyDTO toDelete = apiKeyDAO.getByKey(keyString);

        String activityMsg = "API Key " + toDelete.getApiKey() + " was revoked.";

        apiKeyDAO.delete(toDelete.getId());
        activityManager.addActivity(ActivityConcept.API_KEY, toDelete.getId(), activityMsg, toDelete,
                null);
    }

    @Transactional
    public ApiKeyDTO findKey(final Long keyId) throws EntityRetrievalException {
        return apiKeyDAO.getById(keyId);
    }

    @Transactional
    public ApiKeyDTO findKey(final String keyString) throws EntityRetrievalException {
        return apiKeyDAO.getByKey(keyString);
    }

    @Transactional
    public void logApiKeyActivity(final String keyString, final String apiCallPath)
            throws EntityRetrievalException, EntityCreationException {

        ApiKeyDTO apiKey = findKey(keyString);
        ApiKeyActivityDTO apiKeyActivityDto = new ApiKeyActivityDTO();

        apiKeyActivityDto.setApiCallPath(apiCallPath);
        apiKeyActivityDto.setApiKeyId(apiKey.getId());
        apiKeyActivityDto.setDeleted(false);

        apiKeyActivityDAO.create(apiKeyActivityDto);

        // Update the lastUsedDate...
        apiKey.setLastUsedDate(new Date());
        apiKeyDAO.update(apiKey);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ApiKeyActivity> getApiKeyActivity(final String keyString) throws EntityRetrievalException {

        ApiKeyDTO apiKey = findKey(keyString);
        if (apiKey == null) {
            apiKey = apiKeyDAO.getRevokedKeyByKey(keyString);
        }

        List<ApiKeyActivityDTO> activityDTOs = apiKeyActivityDAO.findByKeyId(apiKey.getId());
        return getApiKeyActivities(activityDTOs);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ApiKeyActivity> getApiKeyActivity(final String keyString,
            final Integer pageNumber, final Integer pageSize) throws EntityRetrievalException {

        ApiKeyDTO apiKey = findKey(keyString);
        if (apiKey == null) {
            apiKey = apiKeyDAO.getRevokedKeyByKey(keyString);
        }

        List<ApiKeyActivityDTO> activityDTOs = apiKeyActivityDAO.findByKeyId(apiKey.getId(), pageNumber, pageSize);
        return getApiKeyActivities(activityDTOs);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ApiKeyDTO> findAll(final Boolean includeDeleted) {
        return apiKeyDAO.findAll(includeDeleted);
    }

    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ApiKeyActivity> getApiKeyActivity() throws EntityRetrievalException {
        List<ApiKeyActivityDTO> activityDTOs = apiKeyActivityDAO.findAll();
        return getApiKeyActivities(activityDTOs);
    }

    /*
     * Gets API key activity within the constraints of the provided parameters
     * Parameters: String apiKeyFilter - string of API key(s) Integer pageNumber
     * - The page for the API key activity Integer pageSize - Number of API keys
     * on the page boolean dateAscending - True if dateAscending; false if
     * dateDescending Long startDate - Start date, in milliseconds, for API key
     * creation Long endDate - End date, in milliseconds, for API key creation
     * Returns: list of ApiKeyActivity
     */
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public List<ApiKeyActivity> getApiKeyActivity(final String apiKeyFilter, final Integer pageNumber,
            final Integer pageSize, final boolean dateAscending, final Long startDateMilli, final Long endDateMilli)
            throws EntityRetrievalException {
        List<ApiKeyActivityDTO> activityDTOs = apiKeyActivityDAO.getApiKeyActivity(apiKeyFilter, pageNumber, pageSize,
                dateAscending, startDateMilli, endDateMilli);

        return getApiKeyActivities(activityDTOs);
    }

    private List<ApiKeyActivity> getApiKeyActivities(List<ApiKeyActivityDTO> activityDTOs) throws EntityRetrievalException {
        List<ApiKeyActivity> apiKeyActivitiesList = new ArrayList<>();
        for (ApiKeyActivityDTO dto : activityDTOs) {

            ApiKeyDTO apiKey = findKey(dto.getApiKeyId());
            if (apiKey == null) {
                apiKey = apiKeyDAO.getRevokedKeyById(dto.getApiKeyId());
            }

            ApiKeyActivity apiKeyActivity = new ApiKeyActivity();

            apiKeyActivity.setApiKey(apiKey.getApiKey());
            apiKeyActivity.setApiKeyId(apiKey.getId());
            apiKeyActivity.setEmail(apiKey.getEmail());
            apiKeyActivity.setName(apiKey.getNameOrganization());
            apiKeyActivity.setId(dto.getId());
            apiKeyActivity.setCreationDate(dto.getCreationDate());
            apiKeyActivity.setApiCallPath(dto.getApiCallPath());

            apiKeyActivitiesList.add(apiKeyActivity);
        }
        return apiKeyActivitiesList;
    }
}
