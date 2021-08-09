package gov.healthit.chpl.api;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.api.dao.ApiKeyDAO;
import gov.healthit.chpl.api.dao.ApiKeyRequestDAO;
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.api.domain.ApiKeyRegistration;
import gov.healthit.chpl.api.domain.ApiKeyRequest;
import gov.healthit.chpl.dao.ApiKeyActivityDAO;
import gov.healthit.chpl.domain.activity.ActivityConcept;
import gov.healthit.chpl.dto.ApiKeyActivityDTO;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.manager.ActivityManager;
import gov.healthit.chpl.util.ErrorMessageUtil;
import gov.healthit.chpl.util.Util;

@Service
public class ApiKeyManager {

    private ApiKeyDAO apiKeyDAO;
    private ApiKeyActivityDAO apiKeyActivityDAO;
    private ActivityManager activityManager;
    private ApiKeyRequestDAO apiKeyRequestDAO;
    private ErrorMessageUtil errorMessages;
    private Environment env;
    private String requestEmailSubject;
    private String requestEmailBody;
    private String confirmEmailSubject;
    private String confirmEmailBody;
    private String chplUrl;

    @Autowired
    @SuppressWarnings("checkstyle:parameternumber")
    public ApiKeyManager(ApiKeyDAO apiKeyDAO, ApiKeyActivityDAO apiKeyActivityDAO, ActivityManager activityManager, ApiKeyRequestDAO apiKeyRequestDAO,
            Environment env, ErrorMessageUtil errorMessages,
            @Value("${apiKey.request.email.subject}") String requestEmailSubject,
            @Value("${apiKey.request.email.body}") String requestEmailBody,
            @Value("${apiKey.confirm.email.subject}") String confirmEmailSubject,
            @Value("${apiKey.confirm.email.body}") String confirmEmailBody,
            @Value("${chplUrlBegin}") String chplUrl) {

        this.apiKeyDAO = apiKeyDAO;
        this.apiKeyActivityDAO = apiKeyActivityDAO;
        this.activityManager = activityManager;
        this.apiKeyRequestDAO = apiKeyRequestDAO;
        this.env = env;
        this.errorMessages = errorMessages;
        this.requestEmailSubject = requestEmailSubject;
        this.requestEmailBody = requestEmailBody;
        this.confirmEmailSubject = confirmEmailSubject;
        this.confirmEmailBody = confirmEmailBody;
        this.chplUrl = chplUrl;
    }

    @Transactional
    public Boolean createRequest(ApiKeyRegistration apiKeyRegistration) throws ValidationException {
        if (!Util.isEmailAddressValidFormat(apiKeyRegistration.getEmail())) {
            throw new ValidationException(String.format("%s is not a valid email address", apiKeyRegistration.getEmail()));
        }

        ApiKeyRequest apiKeyRequest = getApiKeyRequest(apiKeyRegistration);

        EmailBuilder emailBuilder = new EmailBuilder(env);
        try {
            emailBuilder.recipient(apiKeyRequest.getEmail())
                .subject(requestEmailSubject)
                .htmlMessage(String.format(requestEmailBody, apiKeyRequest.getNameOrganization(), chplUrl, apiKeyRequest.getApiRequestToken()))
                .sendEmail();
        } catch (MessagingException e) {
            return false;
        }

        return true;
    }

    @Transactional
    public ApiKey confirmRequest(String token) throws ValidationException, JsonProcessingException, EntityCreationException, EntityRetrievalException, MessagingException {
        Optional<ApiKeyRequest> request = apiKeyRequestDAO.getByApiRequestToken(token);
        if (!request.isPresent()) {
            throw new ValidationException(errorMessages.getMessage("apiKeyRequest.notFound"));
        }

        try {
            apiKeyRequestDAO.delete(request.get().getId());
        } catch (EntityRetrievalException e) {
            throw new ValidationException(errorMessages.getMessage("apiKeyRequest.notFound"));
        }

        ApiKey apiKey = ApiKey.builder()
                .key(generateApiKey(request.get().getNameOrganization(), request.get().getEmail()))
                .email(request.get().getEmail())
                .name(request.get().getNameOrganization())
                .lastUsedDate(new Date())
                .deleteWarningSentDate(null)
                .build();

        apiKey = createKey(apiKey);

        (new EmailBuilder(env))
            .recipient(apiKey.getEmail())
            .subject(confirmEmailSubject)
            .htmlMessage(String.format(confirmEmailBody, apiKey.getName(), apiKey.getKey(), chplUrl))
            .sendEmail();

        return apiKey;
    }

    @Transactional
    public ApiKey createKey(ApiKey toCreate)
            throws EntityCreationException, JsonProcessingException, EntityRetrievalException {

        ApiKey created = apiKeyDAO.create(toCreate);
        String activityMsg = "API Key " + created.getKey() + " was created.";
        activityManager.addActivity(ActivityConcept.API_KEY, created.getId(), activityMsg, null,
                created);
        return created;

    }

    @Transactional
    public ApiKey updateApiKey(ApiKey toUpdate) throws EntityRetrievalException {
        return apiKeyDAO.update(toUpdate);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public void deleteKey(Long keyId) throws EntityRetrievalException, JsonProcessingException, EntityCreationException {
        ApiKey toDelete = apiKeyDAO.getById(keyId);
        String activityMsg = "API Key " + toDelete.getKey() + " was revoked.";
        apiKeyDAO.delete(keyId);
        activityManager.addActivity(ActivityConcept.API_KEY, toDelete.getId(), activityMsg, toDelete,
                null);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ROLE_ADMIN', 'ROLE_ONC')")
    public void deleteKey(String keyString) throws JsonProcessingException, EntityCreationException, EntityRetrievalException {
        ApiKey toDelete = apiKeyDAO.getByKey(keyString);
        String activityMsg = "API Key " + toDelete.getKey() + " was revoked.";
        apiKeyDAO.delete(toDelete.getId());
        activityManager.addActivity(ActivityConcept.API_KEY, toDelete.getId(), activityMsg, toDelete,
                null);
    }

    @Transactional
    public ApiKey findKey(Long keyId) throws EntityRetrievalException {
        return apiKeyDAO.getById(keyId);
    }

    @Transactional
    public ApiKey findKey(String keyString) throws EntityRetrievalException {
        return apiKeyDAO.getByKey(keyString);
    }

    @Transactional
    public void logApiKeyActivity(String keyString, String apiCallPath, String apiCallMethod)
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
    public List<ApiKey> findAll(Boolean includeDeleted) {
        return apiKeyDAO.findAll(includeDeleted);
    }

    private ApiKeyRequest getApiKeyRequest(ApiKeyRegistration apiKeyRegistration) {
        //If API key request already exists for email address, use that one...
        Optional<ApiKeyRequest> existingRequest = apiKeyRequestDAO.getByEmail(apiKeyRegistration.getEmail());
        if (existingRequest.isPresent()) {
            return existingRequest.get();
        } else {
            return apiKeyRequestDAO.create(apiKeyRegistration);
        }
    }

    private String generateApiKey(String nameOrOrganization, String email) {
        Date now = new Date();
        return Util.md5(nameOrOrganization + email + now.getTime());
    }
}
