package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;

import gov.healthit.chpl.api.ApiKeyManager;
import gov.healthit.chpl.api.domain.ApiKeyDTO;
import gov.healthit.chpl.api.domain.ApiKeyRegistration;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.util.EmailBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@Api(value = "api-key")
@RestController
@RequestMapping("/key")
@Loggable
public class ApiKeyController {

    @Autowired
    private ApiKeyManager apiKeyManager;

    @Autowired
    private Environment env;

    @ApiOperation(value = "Sign up for a new API key.",
            notes = "Anyone wishing to access the methods listed in this API must have an API key. This service "
                    + " will auto-generate a key and send it to the supplied email address. It must be included "
                    + " in subsequent API calls via either a header with the name 'API-Key' or as a URL parameter"
                    + " named 'api_key'.")
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = "application/json; charset=utf-8")
    public String register(@RequestBody final ApiKeyRegistration registration) throws EntityCreationException,
    AddressException, MessagingException, JsonProcessingException, EntityRetrievalException {

        return create(registration);
    }

    private String create(final ApiKeyRegistration registration) throws EntityCreationException,
    AddressException, MessagingException, JsonProcessingException, EntityRetrievalException {

        Date now = new Date();

        String apiKey = gov.healthit.chpl.util.Util.md5(registration.getName()
                + registration.getEmail() + now.getTime());
        ApiKeyDTO toCreate = new ApiKeyDTO();

        toCreate.setApiKey(apiKey);
        toCreate.setEmail(registration.getEmail());
        toCreate.setNameOrganization(registration.getName());
        toCreate.setCreationDate(now);
        toCreate.setLastUsedDate(now);
        toCreate.setLastModifiedDate(now);
        toCreate.setLastModifiedUser(-3L);
        toCreate.setDeleted(false);

        apiKeyManager.createKey(toCreate);

        sendRegistrationEmail(registration.getEmail(), registration.getName(), apiKey);

        return "{\"keyRegistered\" : \"" + apiKey + "\"}";
    }

    @ApiOperation(value = "Remove an API key.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC")
    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public String revoke(@PathVariable("key") final String key,
            @RequestHeader(value = "API-Key", required = false) final String userApiKey,
            @RequestParam(value = "apiKey", required = false) final String userApiKeyParam) throws Exception {

        return delete(key, userApiKey, userApiKeyParam);
    }

    private String delete(final String key, final String userApiKey, final String userApiKeyParam) throws Exception {

        String keyToRevoke = key;
        if (keyToRevoke.equals(userApiKey) || keyToRevoke.equals(userApiKeyParam)) {
            throw new Exception("A user can not delete their own API key.");
        }
        apiKeyManager.deleteKey(keyToRevoke);
        return "{\"keyRevoked\" : \"" + keyToRevoke + "\"}";

    }

    @ApiOperation(value = "List all API keys that have been created.",
            notes = "Security Restrictions: ROLE_ADMIN or ROLE_ONC")
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ApiKeyDTO> listKeys(@RequestParam(required = false, defaultValue = "false") final boolean includeDeleted) {

        List<ApiKeyDTO> keys = new ArrayList<ApiKeyDTO>();
        List<ApiKeyDTO> dtos = apiKeyManager.findAll(includeDeleted);

        for (ApiKeyDTO dto : dtos) {
            ApiKeyDTO apiKey = new ApiKeyDTO();
            apiKey.setName(dto.getNameOrganization());
            apiKey.setEmail(dto.getEmail());
            apiKey.setKey(dto.getApiKey());
            apiKey.setLastUsedDate(dto.getLastUsedDate());
            apiKey.setDeleteWarningSentDate(dto.getDeleteWarningSentDate());
            keys.add(apiKey);
        }

        return keys;
    }

    private void sendRegistrationEmail(final String email, final String orgName, final String apiKey)
            throws AddressException, MessagingException {

        String subject = env.getProperty("registrationEmailSubject");
        String htmlMessage = String.format(env.getProperty("registrationEmailBody"),
                apiKey, env.getProperty("chplUrlBegin"));

        String[] toEmails = {
                email
        };

        EmailBuilder emailBuilder = new EmailBuilder(env);
        emailBuilder.recipients(new ArrayList<String>(Arrays.asList(toEmails)))
        .subject(subject)
        .htmlMessage(htmlMessage)
        .sendEmail();
    }
}
