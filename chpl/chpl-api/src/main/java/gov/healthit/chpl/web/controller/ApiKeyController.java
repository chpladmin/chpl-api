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
import gov.healthit.chpl.api.domain.ApiKey;
import gov.healthit.chpl.api.domain.ApiKeyDTO;
import gov.healthit.chpl.api.domain.ApiKeyRegistration;
import gov.healthit.chpl.auth.user.User;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.util.EmailBuilder;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;

@Api(value = "api-key")
@RestController
@RequestMapping("/key")
@Loggable
@Log4j2
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
    public String register(@RequestBody ApiKeyRegistration registration) throws EntityCreationException,
    AddressException, MessagingException, JsonProcessingException, EntityRetrievalException {

        return create(registration);
    }

    private String create(final ApiKeyRegistration registration) throws JsonProcessingException, EntityCreationException,
            EntityRetrievalException, AddressException, MessagingException  {

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
        toCreate.setLastModifiedUser(User.SYSTEM_USER_ID);
        toCreate.setDeleted(false);

        apiKeyManager.createKey(toCreate);

        sendRegistrationEmail(registration.getEmail(), registration.getName(), apiKey);

        return "{\"keyRegistered\" : \"" + apiKey + "\"}";
    }

    @ApiOperation(value = "Sends an email validation to user requesting a new API key.",
            notes = "Anyone wishing to access the methods listed in this API must have an API key. This request "
                      + "will create an email invitation and send it to the supplied email address.  The "
                      + "purpose of the invitation is to validate the email address of the potential API user.")
    @RequestMapping(value = "/request", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public String request(@RequestBody ApiKeyRegistration registration) throws ValidationException {
        return "{\"success\" : \"" + apiKeyManager.createRequest(registration).toString() + "\"}";
    }

    @ApiOperation(value = "Confirms a user's email address and provide the new API key.",
            notes = "Anyone wishing to access the methods listed in this API must have an API key. This service "
                    + "will validate that the user has provided a valid email address and provide them with a new "
                    + "API key. It must be included in subsequent API calls via either a header with the name "
                    + "'API-Key' or as a URL parameter named 'api_key'.")
    @RequestMapping(value = "/confirm", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public ApiKeyDTO confirm(@RequestBody String apiKeyRequestToken) throws JsonProcessingException, ValidationException, EntityCreationException, EntityRetrievalException, MessagingException {
        return apiKeyManager.confirmRequest(apiKeyRequestToken);
    }

    @ApiOperation(value = "Remove an API key.",
            notes = "Security Restrictions: ROLE_ADMIN, ROLE_ONC")
    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE,
    produces = "application/json; charset=utf-8")
    public String revoke(@PathVariable("key") final String key,
            @RequestHeader(value = "API-Key", required = false) String userApiKey,
            @RequestParam(value = "apiKey", required = false) String userApiKeyParam) throws Exception {

        return delete(key, userApiKey, userApiKeyParam);
    }

    private String delete(String key, String userApiKey, String userApiKeyParam) throws Exception {

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
    public List<ApiKey> listKeys(@RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {

        List<ApiKey> keys = new ArrayList<ApiKey>();
        List<ApiKeyDTO> dtos = apiKeyManager.findAll(includeDeleted);

        for (ApiKeyDTO dto : dtos) {
            ApiKey apiKey = new ApiKey();
            apiKey.setName(dto.getNameOrganization());
            apiKey.setEmail(dto.getEmail());
            apiKey.setKey(dto.getApiKey());
            apiKey.setLastUsedDate(dto.getLastUsedDate());
            apiKey.setDeleteWarningSentDate(dto.getDeleteWarningSentDate());
            keys.add(apiKey);
        }

        return keys;
    }

    private void sendRegistrationEmail(String email, String orgName, String apiKey)
            throws AddressException, MessagingException {

        String subject = env.getProperty("apiKey.confirm.email.subject");
        String htmlMessage = String.format(env.getProperty("apiKey.confirm.email.body"),
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
