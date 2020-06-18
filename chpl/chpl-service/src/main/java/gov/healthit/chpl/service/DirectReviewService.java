package gov.healthit.chpl.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.compliance.DirectReview;
import gov.healthit.chpl.exception.InvalidArgumentsException;
import gov.healthit.chpl.util.ChplProductNumberUtil;
import gov.healthit.chpl.util.ChplProductNumberUtil.ChplProductNumberParts;
import gov.healthit.chpl.util.ErrorMessageUtil;
import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class DirectReviewService {
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
        String jsonResult = "";
        //The actual code will replace what is below, which is just selecting a sample JSON file
        //to serialize and return.
        File sampleFile = getSampleFile("jira-" + chplProductNumber + ".json");
        if (sampleFile != null && sampleFile.exists()) {
                jsonResult = readFileContents(sampleFile);
        } else {
            LOGGER.info("Sample file cannot be found. Sending back empty direct review results.");
            File noResultsSampleFile = getSampleFile("jira-no-results.json");
            if (noResultsSampleFile != null && noResultsSampleFile.exists()) {
                jsonResult = readFileContents(noResultsSampleFile);
            } else {
                LOGGER.fatal("jira-no-results.json could not be found on the classpath.");
                return null;
            }
        }

        return convertFromJira(jsonResult);
    }

    //TODO: modify this method to convert all Jira json into CHPL json
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

    //
    //TODO: all below methods will be thrown away once we connect to Jira
    //

    private File getSampleFile(String filename) {
        return new File(getClass().getResource("directReviews" + File.separator + filename).getFile());
    }

    private String readFileContents(File file) {
        String contents = "";
        try {
            contents = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return contents;
    }
}
