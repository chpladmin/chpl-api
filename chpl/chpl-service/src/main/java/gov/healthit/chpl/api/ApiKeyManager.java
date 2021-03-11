package gov.healthit.chpl.api;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.api.dao.ApiKeyDAO;
import gov.healthit.chpl.api.dao.ApiKeyRequestDAO;
import gov.healthit.chpl.api.domain.ApiKeyDTO;
import gov.healthit.chpl.api.domain.ApiKeyRegistration;
import gov.healthit.chpl.api.domain.ApiKeyRequest;
import gov.healthit.chpl.dao.ApiKeyActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.util.EmailBuilder;
import gov.healthit.chpl.util.Util;

@Service
public class ApiKeyManager {

    private ApiKeyDAO apiKeyDAO;
    private ApiKeyActivityDAO apiKeyActivityDAO;
    private ActivityManager activityManager;
    private ApiKeyRequestDAO apiKeyRequestDAO;
    private Environment env;

    @Autowired
    public ApiKeyManager(ApiKeyDAO apiKeyDAO, ApiKeyActivityDAO apiKeyActivityDAO,
        ActivityManager activityManager, ApiKeyRequestDAO apiKeyRequestDAO, Environment env) {
        this.apiKeyDAO = apiKeyDAO;
        this.apiKeyActivityDAO = apiKeyActivityDAO;
        this.activityManager = activityManager;
        this.apiKeyRequestDAO = apiKeyRequestDAO;
        this.env = env;
    }

    @Transactional
    public Boolean sendInvitation(ApiKeyRegistration apiKeyRegistration) throws ValidationException {
        if (!Util.isEmailAddressValidFormat(apiKeyRegistration.getEmail())) {
            throw new ValidationException(String.format("%s is not a valid email address", apiKeyRegistration.getEmail()));
        }

        //If API key request already exists for email address, use that one...
        Optional<ApiKeyRequest> existingRequest = apiKeyRequestDAO.getByEmail(apiKeyRegistration.getEmail());
        ApiKeyRequest apiKeyRequest = null;
        if (existingRequest.isPresent()) {
            apiKeyRequest = existingRequest.get();
        } else {
            apiKeyRequest = apiKeyRequestDAO.create(apiKeyRegistration);
        }

        EmailBuilder emailBuilder = new EmailBuilder(env);
        try {
            emailBuilder.recipient(apiKeyRequest.getEmail())
                .htmlMessage("Here is your token: " + apiKeyRequest.getApiRequestToken())
                .sendEmail();
        } catch (MessagingException e) {
            return false;
        }

        return true;
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
    public void logApiKeyActivity(final String keyString, final String apiCallPath, final String apiCallMethod)
            throws EntityRetrievalException, EntityCreationException {

        ApiKeyDTO apiKey = findKey(keyString);
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
    public List<ApiKeyDTO> findAll(final Boolean includeDeleted) {
        return apiKeyDAO.findAll(includeDeleted);
    }


}
