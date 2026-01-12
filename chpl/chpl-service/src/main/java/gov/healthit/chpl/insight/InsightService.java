package gov.healthit.chpl.insight;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import gov.healthit.chpl.dao.DeveloperDAO;
import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.exception.EntityRetrievalException;
import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

@Log4j2
@Component
public class InsightService {
    private DeveloperDAO developerDao;
    private RestTemplate insightRestTemplate;
    private JsonMapper jsonMapper;
    private String unformattedInsightSubmissionsUrl;

    @Autowired
    public InsightService(DeveloperDAO developerDao,
            JsonMapper jsonMapper,
            @Value("${insight.submissionsUrl}") String insightSubmissionsUrl) {
        this.developerDao = developerDao;
        this.insightRestTemplate = new RestTemplate(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        this.jsonMapper = jsonMapper;
        this.unformattedInsightSubmissionsUrl = insightSubmissionsUrl;
    }

    @Transactional
    public List<InsightSubmission> getInsightSubmissions(Long developerId) throws EntityRetrievalException, InsightRequestFailedException {
        Developer dev = developerDao.getById(developerId); // throws exception if invalid ID
        JsonNode jsonResults = fetchInsightSubmissionsForDeveloper(dev.getId());
        return convertInsightSubmissionsResponse(dev.getId(), jsonResults);
    }

    private JsonNode fetchInsightSubmissionsForDeveloper(Long developerId) throws InsightRequestFailedException {
        String url = String.format(unformattedInsightSubmissionsUrl, developerId.toString());
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = insightRestTemplate.getForEntity(url, String.class);
            LOGGER.debug("Response: " + response.getBody());
            if (response == null || StringUtils.isEmpty(response.getBody())) {
                LOGGER.warn("A null or empty response was received from the Insights API.");
            }
        } catch (Exception ex) {
            HttpStatusCode statusCode =  (response != null ? response.getStatusCode() : null);
            if (statusCode == null && ex instanceof RestClientResponseException) {
                statusCode = ((RestClientResponseException) ex).getStatusCode();
            }
            LOGGER.error("Unable to connect to the URL " + url + ". Got response status code " + statusCode);
            throw new InsightRequestFailedException(ex.getMessage(), ex, statusCode);
        }
        String responseBody = ((response == null || StringUtils.isEmpty(response.getBody())) ? "{}" : response.getBody());
        JsonNode root = null;
        try {
            root = jsonMapper.readTree(responseBody);
        } catch (JacksonException ex) {
            LOGGER.error("Could not convert " + responseBody + " to JsonNode object.", ex);
            throw new InsightRequestFailedException("Could not convert " + responseBody + " to expected object.", ex);
        }
        return root;
    }

    private List<InsightSubmission> convertInsightSubmissionsResponse(Long developerId, JsonNode rootNode) {
        List<InsightSubmission> submissions = new ArrayList<InsightSubmission>();
        if (rootNode != null && rootNode.isArray() && rootNode.size() > 0) {
            for (JsonNode submissionObj : rootNode) {
                try {
                    InsightSubmission is = jsonMapper.readValue(submissionObj.toString(), InsightSubmission.class);
                    submissions.add(is);
                } catch (JacksonException ex) {
                    LOGGER.error("Cannot map submission JSON to InsightSubmission class", ex);
                }
            }
        }
        return submissions;
    }
}

