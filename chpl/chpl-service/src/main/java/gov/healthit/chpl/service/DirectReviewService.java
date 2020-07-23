package gov.healthit.chpl.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.domain.compliance.DirectReviewNonconformity;
import lombok.extern.log4j.Log4j2;

@Component("directReviewService")
@Log4j2
public class DirectReviewService {
    private static final String JIRA_KEY_FIELD = "key";
    private static final String JIRA_ISSUES_FIELD = "issues";
    private static final String JIRA_FIELDS_FIELD = "fields";
    private ObjectMapper mapper;

    public DirectReviewService() {
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
        //Code to query jira goes here.
        //The actual code will replace what is below, which is just selecting a sample JSON file
        //to serialize and return.

        String jsonResult = "";
        String sampleFileName = developerId + ".json";
        Resource resource = new ClassPathResource("directReviews/" + sampleFileName);
        InputStream sampleJsonInputStream = null;
        try {
            sampleJsonInputStream = resource.getInputStream();
            if (sampleJsonInputStream != null) {
                jsonResult = IOUtils.toString(sampleJsonInputStream);
            }
        } catch (IOException ex) {
            LOGGER.warn("Can't find resource " + sampleFileName);
        }

        if (StringUtils.isEmpty(jsonResult)) {
            LOGGER.info("Sample file cannot be found. Sending back empty direct review results.");
            String defaultSampleFile = "directReviews/no-results.json";
            resource = new ClassPathResource(defaultSampleFile);
            InputStream noResultsInputStream = null;
            try {
                noResultsInputStream = resource.getInputStream();
                if (noResultsInputStream != null) {
                    jsonResult = IOUtils.toString(noResultsInputStream);
                }
            } catch (IOException ex) {
                LOGGER.fatal("Can't find resource " + defaultSampleFile, ex);
            }
        }
        return jsonResult;
    }

    private String fetchNonconformities(String directReviewKey) {
        //Code to query jira goes here.
        //The actual code will replace what is below, which is just selecting a sample JSON file
        //to serialize and return.

        String jsonResult = "";
        String sampleFileName = directReviewKey + "-nonconformities.json";
        Resource resource = new ClassPathResource("directReviews/" + sampleFileName);
        InputStream sampleJsonInputStream = null;
        try {
            sampleJsonInputStream = resource.getInputStream();
            if (sampleJsonInputStream != null) {
                jsonResult = IOUtils.toString(sampleJsonInputStream);
            }
        } catch (IOException ex) {
            LOGGER.warn("Can't find resource " + sampleFileName);
        }

        if (StringUtils.isEmpty(jsonResult)) {
            LOGGER.info("Sample file cannot be found. Sending back empty direct review results.");
            String defaultSampleFile = "directReviews/no-results.json";
            resource = new ClassPathResource(defaultSampleFile);
            InputStream noResultsInputStream = null;
            try {
                noResultsInputStream = resource.getInputStream();
                if (noResultsInputStream != null) {
                    jsonResult = IOUtils.toString(noResultsInputStream);
                }
            } catch (IOException ex) {
                LOGGER.fatal("Can't find resource " + defaultSampleFile, ex);
            }
        }
        return jsonResult;
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
            if (issuesNode.isArray() && issuesNode.size() > 0) {
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
            if (issuesNode.isArray() && issuesNode.size() > 0) {
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
