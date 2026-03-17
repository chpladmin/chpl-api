package gov.healthit.chpl.astpai;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@Service
public class AstpAiAuthenticationService {

    private RestTemplate httpsRestTemplate;
    private String authenticationUrl;
    private String authenticationRequestBody;
    private JsonMapper jsonMapper;

    @Autowired
    public AstpAiAuthenticationService(RestTemplate httpsRestTemplate,
            JsonMapper jsonMapper,
            @Value("${astpai.authenticate.url}") String authenticationUrl,
            @Value("${astpai.authenticate.clientSecret}") String authenticationClientSecret,
            @Value("${astpai.authenticate.clientId}") String authenticationClientId) {
        this.httpsRestTemplate = httpsRestTemplate;
        this.jsonMapper = jsonMapper;
        this.authenticationUrl = authenticationUrl;
        this.authenticationRequestBody = String.format("grant_type=client_credentials&scope=default-m2m-resource-server-p3thsy/read&client_id=%s&client_secret=%s", authenticationClientId, authenticationClientSecret);
    }

    public AmazonTokenResponse authenticate() throws AstpAiRequestFailedException {
        LOGGER.info("Making request to " + authenticationUrl);
        ResponseEntity<String> response = null;
        try {
            LOGGER.debug("Request body:" + authenticationRequestBody);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Content-Type", "application/x-www-form-urlencoded");
            headers.add("Accept", "application/json");
            headers.add("Accept-Encoding", "UTF-8");
            HttpEntity<String> entity = new HttpEntity<>(authenticationRequestBody, headers);

            response = httpsRestTemplate.exchange(authenticationUrl, HttpMethod.POST, entity, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (HttpClientErrorException httpEx) {
            LOGGER.error("Unable to authenticate with the URL " + authenticationUrl + ". Message: " + httpEx.getMessage() + "; response status code " + httpEx.getStatusCode());
            throw new AstpAiRequestFailedException(httpEx.getMessage(), httpEx, httpEx.getStatusCode());
        } catch (Exception ex) {
            HttpStatusCode statusCode =  (response != null && response.getStatusCode() != null
                    ? response.getStatusCode() : HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            LOGGER.error("Unable to authenticate with the URL " + authenticationUrl + ". Message: " + ex.getMessage() + "; response status code " + statusCode);
            throw new AstpAiRequestFailedException(ex.getMessage(), ex, statusCode);
        }

        String responseBody = response == null ? "" : response.getBody();
        AmazonTokenResponse token = null;
        try {
            token = jsonMapper.readValue(responseBody, AmazonTokenResponse.class);
        } catch (JacksonException ex) {
            LOGGER.error("Unable to read the response body as our custom AmazonTokenResponse", ex);
            throw new AstpAiRequestFailedException(ex.getMessage(), ex);
        }
        return token;
    }
}
