package gov.healthit.chpl.aia;

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
public class AIAQueryService {

    private RestTemplate httpsRestTemplate;
    private String rwtResultsValidationUrl;
    private JsonMapper jsonMapper;

    @Autowired
    public AIAQueryService(RestTemplate httpsRestTemplate,
            JsonMapper jsonMapper,
            @Value("${aia.domain}") String aiaDomain,
            @Value("${aia.rwtResultUrlValidation.endpoint}") String aiaRwtResultValidationApi) {
        this.httpsRestTemplate = httpsRestTemplate;
        this.jsonMapper = jsonMapper;
        this.rwtResultsValidationUrl = aiaDomain + aiaRwtResultValidationApi;
    }

    public UrlValidationResponse getRwtResultsUrlValidationResponse(String accessToken, UrlValidationRequest requestBody)
            throws AIARequestFailedException {
        LOGGER.info("Making request to " + rwtResultsValidationUrl + " with access token " + accessToken);
        ResponseEntity<String> response = null;
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "Bearer " + accessToken);
            headers.add("Accept", "application/json");
            HttpEntity<UrlValidationRequest> entity = new HttpEntity<>(requestBody, headers);

            response = httpsRestTemplate.exchange(rwtResultsValidationUrl, HttpMethod.POST, entity, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (HttpClientErrorException httpEx) {
            LOGGER.error("Unable to query the URL " + rwtResultsValidationUrl + ". Message: " + httpEx.getMessage() + "; response status code " + httpEx.getStatusCode());
            throw new AIARequestFailedException(httpEx.getMessage(), httpEx, httpEx.getStatusCode());
        } catch (Exception ex) {
            HttpStatusCode statusCode =  (response != null && response.getStatusCode() != null
                    ? response.getStatusCode() : HttpStatusCode.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()));
            LOGGER.error("Unable to query the URL " + rwtResultsValidationUrl + ". Message: " + ex.getMessage() + "; response status code " + statusCode);
            throw new AIARequestFailedException(ex.getMessage(), ex, statusCode);
        }

        String responseBody = response == null ? "" : response.getBody();
        UrlValidationResponse aiQueryResponse = null;
        try {
            aiQueryResponse = jsonMapper.readValue(responseBody, UrlValidationResponse.class);
        } catch (JacksonException ex) {
            LOGGER.error("Unable to read the response body as our custom AmazonTokenResponse", ex);
            throw new AIARequestFailedException(ex.getMessage(), ex);
        }
        return aiQueryResponse;
    }
}
