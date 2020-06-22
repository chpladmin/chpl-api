package gov.healthit.chpl.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil.ChplProductNumberParts;
import gov.healthit.chpl.util.ErrorMessageUtil;

@Component("directReviewService")
public class DirectReviewService {
    private static final Logger LOGGER = LogManager.getLogger(DirectReviewService.class);
    private static final String EDITION_2014 = "14";

    private ObjectMapper mapper;
    private ErrorMessageUtil msgUtil;
    private ChplProductNumberUtil chplNumberUtil;

    @Autowired
    public DirectReviewService(ChplProductNumberUtil chplNumberUtil, ErrorMessageUtil msgUtil) {
        this.mapper = new ObjectMapper();
        this.chplNumberUtil = chplNumberUtil;
        this.msgUtil = msgUtil;
    }

    public List<DirectReview> getDirectReviews(String chplProductNumber) throws InvalidArgumentsException {
        LOGGER.info("Fetching direct review data for listing " + chplProductNumber);
        if (chplNumberUtil.isLegacy(chplProductNumber)) {
            LOGGER.warn(msgUtil.getMessage("directReview.legacyNotAllowed"));
            throw new InvalidArgumentsException(msgUtil.getMessage("directReview.legacyNotAllowed"));
        }
        ChplProductNumberParts parts = chplNumberUtil.parseChplProductNumber(chplProductNumber);
        if (parts.getEditionCode().equals(EDITION_2014)) {
            LOGGER.warn(msgUtil.getMessage("directReview.editionNotAllowed", parts.getEditionCode()));
            throw new InvalidArgumentsException(msgUtil.getMessage("directReview.editionNotAllowed", parts.getEditionCode()));
        }

        //Code to query jira goes here.
        //Example of how to query by CHPL ID: /search?jql=cf[10213]~15.04.04.2858.Tsys.06.01.1.191223
        //The actual code will replace what is below, which is just selecting a sample JSON file
        //to serialize and return.

        String jsonResult = "";
        String dashedChplProductNumber = chplProductNumber.replaceAll("\\.", "-");
        String sampleFileName = "jira-" + dashedChplProductNumber + ".json";
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
            String defaultSampleFile = "directReviews/jira-no-results.json";
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
