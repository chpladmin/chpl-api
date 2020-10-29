package gov.healthit.chpl.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.caching.CacheNames;
import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;
import gov.healthit.chpl.exception.JiraRequestFailedException;
import lombok.extern.log4j.Log4j2;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

@Component("directReviewService")
@Log4j2
public class DirectReviewService {
    private static final String JIRA_KEY_FIELD = "key";
    private static final String JIRA_ISSUES_FIELD = "issues";
    private static final String JIRA_FIELDS_FIELD = "fields";

    @Value("${jira.baseUrl}")
    private String jiraBaseUrl;

    @Value("${jira.directReviewsUrl}")
    private String jiraAllDirectReviewsUrl;

    @Value("${jira.directReviewsForDeveloperUrl}")
    private String jiraDirectReviewsForDeveloperUrl;

    @Value("${jira.nonconformityUrl}")
    private String jiraNonconformityUrl;

    private RestTemplate jiraAuthenticatedRestTemplate;
    private ObjectMapper mapper;

    @Autowired
    public DirectReviewService(RestTemplate jiraAuthenticatedRestTemplate) {
        this.jiraAuthenticatedRestTemplate = jiraAuthenticatedRestTemplate;
        this.mapper = new ObjectMapper();
    }

    public List<DirectReview> getDirectReviews() throws JiraRequestFailedException {
        LOGGER.info("Fetching all direct review data.");

        String directReviewsJson = fetchDirectReviews();
        List<DirectReview> drs = convertDirectReviewsFromJira(directReviewsJson);
        List<Element> drCacheElements = new ArrayList<Element>(drs.size());
        for (DirectReview dr : drs) {
            String nonConformitiesJson = fetchNonConformities(dr.getJiraKey());
            List<DirectReviewNonConformity> ncs = convertNonConformitiesFromJira(nonConformitiesJson);
            if (ncs != null && ncs.size() > 0) {
                dr.getNonConformities().addAll(ncs);
            }
            drCacheElements.add(new Element(dr.getDeveloperId(), dr));
        }

        CacheManager manager = CacheManager.getInstance();
        Cache drCache = manager.getCache(CacheNames.DIRECT_REVIEWS);
        LOGGER.info("Put " + drCacheElements + " into the Direct Review cache.");
        drCache.putAll(drCacheElements);
        return drs;
    }

    @Cacheable(CacheNames.DIRECT_REVIEWS)
    public List<DirectReview> getDirectReviews(Long developerId) throws JiraRequestFailedException {
        LOGGER.info("Fetching direct review data for developer " + developerId);

        String directReviewsJson = fetchDirectReviews(developerId);
        List<DirectReview> drs = convertDirectReviewsFromJira(directReviewsJson);
        for (DirectReview dr : drs) {
            String nonConformitiesJson = fetchNonConformities(dr.getJiraKey());
            List<DirectReviewNonConformity> ncs = convertNonConformitiesFromJira(nonConformitiesJson);
            if (ncs != null && ncs.size() > 0) {
                dr.getNonConformities().addAll(ncs);
            }
        }
        return drs;
    }

    private String fetchDirectReviews() throws JiraRequestFailedException {
        String url = jiraBaseUrl + jiraAllDirectReviewsUrl;
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            throw new JiraRequestFailedException(ex.getMessage());
        }
        return response == null ? "" : response.getBody();
    }

    private String fetchDirectReviews(Long developerId) throws JiraRequestFailedException {
        String url = String.format(jiraBaseUrl + jiraDirectReviewsForDeveloperUrl, developerId + "");
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            throw new JiraRequestFailedException(ex.getMessage());
        }
        return response == null ? "" : response.getBody();
    }

    private String fetchNonConformities(String directReviewKey) throws JiraRequestFailedException {
        String url = String.format(jiraBaseUrl + jiraNonconformityUrl, directReviewKey + "");
        LOGGER.info("Making request to " + url);
        ResponseEntity<String> response = null;
        try {
            response = jiraAuthenticatedRestTemplate.getForEntity(url, String.class);
            LOGGER.debug("Response: " + response.getBody());
        } catch (Exception ex) {
            throw new JiraRequestFailedException(ex.getMessage());
        }
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

    private List<DirectReviewNonConformity> convertNonConformitiesFromJira(String json) {
        List<DirectReviewNonConformity> ncs = new ArrayList<DirectReviewNonConformity>();
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
                        DirectReviewNonConformity nc = mapper.readValue(fieldsJson, DirectReviewNonConformity.class);
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
