package gov.healthit.chpl.web.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

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
import gov.healthit.chpl.api.domain.ApiKeyRegistration;
import gov.healthit.chpl.email.EmailBuilder;
import gov.healthit.chpl.exception.EmailNotSentException;
import gov.healthit.chpl.exception.EntityCreationException;
import gov.healthit.chpl.exception.EntityRetrievalException;
import gov.healthit.chpl.exception.ValidationException;
import gov.healthit.chpl.logging.Loggable;
import gov.healthit.chpl.util.SwaggerSecurityRequirement;
import gov.healthit.chpl.web.controller.annotation.DeprecatedResponseFields;
import gov.healthit.chpl.web.controller.results.BooleanResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;

@Tag(name = "api-key", description = "Allows CRUD operations on API Keys.")
@RestController
@RequestMapping("/key")
@Loggable
@Log4j2
public class ApiKeyController {

    @Autowired
    private ApiKeyManager apiKeyManager;

    @Autowired
    private Environment env;

    @Operation(summary = "Sign up for a new API key.",
        description = "Anyone wishing to access the methods listed in this API must have an API key. This service "
                + " will auto-generate a key and send it to the supplied email address. It must be included "
                + " in subsequent API calls via either a header with the name 'API-Key' or as a URL parameter"
                + " named 'api_key'.",
        security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = "application/json; charset=utf-8")
    public KeyRegistered register(@RequestBody ApiKeyRegistration registration) throws EntityCreationException,
    AddressException, EmailNotSentException, JsonProcessingException, EntityRetrievalException {

        return create(registration);
    }

    private KeyRegistered create(final ApiKeyRegistration registration) throws JsonProcessingException, EntityCreationException,
            EntityRetrievalException, AddressException, EmailNotSentException  {
        Date now = new Date();
        String apiKey = gov.healthit.chpl.util.Util.md5(registration.getName() + registration.getEmail() + now.getTime());
        ApiKey toCreate = ApiKey.builder()
                .key(apiKey)
                .email(registration.getEmail())
                .name(registration.getName())
                .unrestricted(false)
                .build();

        apiKeyManager.createKey(toCreate);
        sendRegistrationEmail(registration.getEmail(), registration.getName(), apiKey);
        return new KeyRegistered(apiKey);
    }

    @Operation(summary = "Sends an email validation to user requesting a new API key.",
        description = "Anyone wishing to access the methods listed in this API must have an API key. This request "
                  + "will create an email invitation and send it to the supplied email address. The "
                  + "purpose of the invitation is to validate the email address of the potential API user.",
          security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @RequestMapping(value = "/request", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public BooleanResult request(@RequestBody ApiKeyRegistration registration)
            throws ValidationException, EmailNotSentException {
        return new BooleanResult(apiKeyManager.createRequest(registration));
    }

    @Operation(summary = "Confirms a user's email address and provides the new API key.",
        description = "Anyone wishing to access the methods listed in this API must have an API key. This service "
                + "will validate that the user has provided a valid email address and provide them with a new "
                + "API key. It must be included in subsequent API calls via either a header with the name "
                + "'API-Key' or as a URL parameter named 'api_key'.",
        security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY)})
    @DeprecatedResponseFields(responseClass = ApiKey.class)
    @RequestMapping(value = "/confirm", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = "application/json; charset=utf-8")
    public ApiKey confirm(@RequestBody String apiKeyRequestToken) throws
        JsonProcessingException, ValidationException, EntityCreationException,
        EntityRetrievalException, EmailNotSentException {
        return apiKeyManager.confirmRequest(apiKeyRequestToken);
    }

    @Operation(summary = "Remove an API key.",
            description = "Security Restrictions: ROLE_ADMIN, ROLE_ONC",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER) })
    @RequestMapping(value = "/{key}", method = RequestMethod.DELETE, produces = "application/json; charset=utf-8")
    public KeyRevoked revoke(@PathVariable("key") final String key,
            @RequestHeader(value = "API-Key", required = false) String userApiKey,
            @RequestParam(value = "apiKey", required = false) String userApiKeyParam) throws Exception {
        return delete(key, userApiKey, userApiKeyParam);
    }

    private KeyRevoked delete(String key, String userApiKey, String userApiKeyParam) throws Exception {
        String keyToRevoke = key;
        if (keyToRevoke.equals(userApiKey) || keyToRevoke.equals(userApiKeyParam)) {
            throw new Exception("A user can not delete their own API key.");
        }
        apiKeyManager.deleteKey(keyToRevoke);
        return new KeyRevoked(keyToRevoke);

    }

    @Operation(summary = "List all API keys that have been created.",
            description = "Security Restrictions: ROLE_ADMIN or ROLE_ONC",
            security = { @SecurityRequirement(name = SwaggerSecurityRequirement.API_KEY),
                    @SecurityRequirement(name = SwaggerSecurityRequirement.BEARER) })
    @DeprecatedResponseFields(responseClass = ApiKey.class)
    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json; charset=utf-8")
    public List<ApiKey> listKeys(@RequestParam(required = false, defaultValue = "false") boolean includeDeleted) {
        return apiKeyManager.findAll(includeDeleted);
    }

    private void sendRegistrationEmail(String email, String orgName, String apiKey)
            throws AddressException, EmailNotSentException {

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

    private class KeyRegistered {
        private String keyRegistered;

        KeyRegistered(String keyRegistered) {
            this.keyRegistered = keyRegistered;
        }

       public String getKeyRegistered() {
            return keyRegistered;
       }
    }

    private class KeyRevoked {
        private String keyRevoked;

        KeyRevoked(String keyRevoked) {
            this.keyRevoked = keyRevoked;
        }

       public String getKeyRevoked() {
            return keyRevoked;
       }
    }

}
