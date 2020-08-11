package gov.healthit.chpl.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonconformity;
import lombok.extern.log4j.Log4j2;

@Component("directReviewService")
@Log4j2
public class DirectReviewService {
    @SuppressWarnings("checkstyle:linelength")
    private static final String JIRA_DIRECT_REVIEW_URL = "/search?jql=project=\"Review for Signals/Direct Review\" and type=\"Direct Review\" and \"CHPL Developer ID\"~\"%s\" and \"Make Visible to CHPL\"=\"Yes\"";
    private static final String JIRA_NONCONFORMITY_URL = "/search/?jql=project=\"Review for Signals/Direct Review\" and type=\"Requirement/Non-Conformity\" and \"Make Visible to CHPL\"=\"Yes\" and parent=\"%s\"";
    private static final String JIRA_KEY_FIELD = "key";
    private static final String JIRA_ISSUES_FIELD = "issues";
    private static final String JIRA_FIELDS_FIELD = "fields";

    @Value("${jira.baseUrl}")
    private String jiraBaseUrl;

    private RestTemplate jiraAuthenticatedRestTemplate;
    private ObjectMapper mapper;

    @Autowired
    public DirectReviewService(RestTemplate jiraAuthenticatedRestTemplate) {
        this.jiraAuthenticatedRestTemplate = jiraAuthenticatedRestTemplate;
        this.mapper = new ObjectMapper();
    }

    public List<DirectReview> getDirectReviews(Long developerId) {
        LOGGER.info("Fetching direct review data for developer " + developerId);

        String directReviewsJson = fetchJson(developerId);
        List<DirectReview> drs = convertDirectReviewsFromJira(directReviewsJson);
        for (DirectReview dr : drs) {
            String nonconformitiesJson = fetchNonconformities(dr.getJiraKey());
            List<DirectReviewNonconformity> ncs = convertNonconformitiesFromJira(nonconformitiesJson);
            if (ncs != null && ncs.size() > 0) {
                dr.getNonconformities().addAll(ncs);
            }
        }
        return drs;
    }

    private String fetchJson(Long developerId) {
        String url = String.format(jiraBaseUrl + JIRA_DIRECT_REVIEW_URL, developerId + "");
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
        LOGGER.debug("Response: " + response.getBody());
        return response.getBody();
    }

    private String fetchNonconformities(String directReviewKey) {
        String url = String.format(jiraBaseUrl + JIRA_NONCONFORMITY_URL, directReviewKey + "");
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
        LOGGER.debug("Response: " + response.getBody());
        return response.getBody();
    }

    private List<DirectReview> convertDirectReviewsFromJira(String json) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (IOException ex) {
            LOGGER.error("Could not convert " + json + " to JsonNode object.", ex);
        }
        if (root != null) {
            JsonNode issuesNode = root.get(JIRA_ISSUES_FIELD);
            if (issuesNode != null && issuesNode.isArray() && issuesNode.size() > 0) {
                for (JsonNode issueNode : issuesNode) {
                    try {
                        String jiraKey = issueNode.get(JIRA_KEY_FIELD).textValue();
                        JsonNode fields = issueNode.get(JIRA_FIELDS_FIELD);
                        DirectReview dr = mapper.readValue(fields.toString(), DirectReview.class);
                        dr.setJiraKey(jiraKey);
                        drs.add(dr);
                    } catch (IOException ex) {
                        LOGGER.error("Cannot map issue JSON to DirectReview class", ex);
                    }
                }
            }
        }
        return drs;
    }

    private List<DirectReviewNonconformity> convertNonconformitiesFromJira(String json) {
        List<DirectReviewNonconformity> ncs = new ArrayList<DirectReviewNonconformity>();
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (IOException ex) {
            LOGGER.error("Could not convert " + json + " to JsonNode object.", ex);
        }
        if (root != null) {
            JsonNode issuesNode = root.get(JIRA_ISSUES_FIELD);
            if (issuesNode != null && issuesNode.isArray() && issuesNode.size() > 0) {
                for (JsonNode issueNode : issuesNode) {
                    try {
                        String fieldsJson = issueNode.get(JIRA_FIELDS_FIELD).toString();
                        DirectReviewNonconformity nc = mapper.readValue(fieldsJson, DirectReviewNonconformity.class);
                        ncs.add(nc);
                    } catch (IOException ex) {
                        LOGGER.error("Cannot map issue JSON to DirectReviewNonconformity class", ex);
                    }
                }
            }
        }
        return ncs;
    }
}
