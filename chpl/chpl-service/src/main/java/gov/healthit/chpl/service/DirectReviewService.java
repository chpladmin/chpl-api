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
import lombok.extern.log4j.Log4j2;

@Component("directReviewService")
@Log4j2
public class DirectReviewService {
    private ObjectMapper mapper;

    public DirectReviewService() {
        this.mapper = new ObjectMapper();
    }

    public List<DirectReview> getDirectReviews(Long developerId) {
        LOGGER.info("Fetching direct review data for developer " + developerId);
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

        return convertFromJira(jsonResult);
    }

    private List<DirectReview> convertFromJira(String json) {
        List<DirectReview> drs = new ArrayList<DirectReview>();
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (IOException ex) {
            LOGGER.error("Could not convert " + json + " to JsonNode object.", ex);
        }
        if (root != null) {
            JsonNode issuesNode = root.get("issues");
            if (issuesNode.isArray() && issuesNode.size() > 0) {
                for (JsonNode issueNode : issuesNode) {
                    try {
                        String fieldsJson = issueNode.get("fields").toString();
                        DirectReview dr = mapper.readValue(fieldsJson, DirectReview.class);
                        drs.add(dr);
                    } catch (IOException ex) {
                        LOGGER.error("Cannot map issue JSON to DirectReview class", ex);
                    }
                }
            }
        }
        return drs;
    }
}
