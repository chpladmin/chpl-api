package gov.healthit.chpl.directReview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.compliance.DirectReview;

public class DirectReviewDeserializationTest {

    @Test
    public void deserializeJson_parsesKey() {
        String key = "R4SDR-98";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"" + key + "\", "
                + "\"fields\": {}"
                + "}"
                + "]"
                + "}";
        DirectReview dr = parseJsonToDirectReview(json);
        assertNotNull(dr);
        assertNotNull(dr.getJiraKey());
    }

    @Test
    public void deserializeJson_parsesStartDate() {
        String startDate = "2020-06-01T00:00:00.000+0000";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10946\": \"" + startDate + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(json);
        assertNotNull(dr);
        assertNotNull(dr.getStartDate());
    }

    @Test
    public void deserializeJson_parsesEndDate() {
        String endDate = "2020-06-30T12:24:00.000+0000";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10947\": \"" + endDate + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(json);
        assertNotNull(dr);
        assertNotNull(dr.getEndDate());
    }

    @Test
    public void deserializeJson_parsesCircumstances() {
        String circumstanceName = "Noncompliance to Conditions and Maintenance of Certification";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10932\": [{"
                + "  \"self\": \"https://oncprojectracking.ahrqdev.org/support-jsd/rest/api/2/customFieldOption/10718\","
                + "  \"value\": \"" + circumstanceName + "\","
                + "  \"id\": \"10718\""
                + "}]"
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(json);
        assertNotNull(dr);
        assertNotNull(dr.getCircumstances());
        assertEquals(1, dr.getCircumstances().size());
        assertEquals(circumstanceName, dr.getCircumstances().get(0));
    }

    @Test
    public void deserializeJson_parsesLastUpdatedDate() {
        String lastUpdatedDate = "2020-06-18T13:08:51.000+0000";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"updated\": \"" + lastUpdatedDate + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(json);
        assertNotNull(dr);
        assertNotNull(dr.getLastUpdated());
    }

    @Test
    public void deserializeJson_parsesCreatedDate() {
        String createdDate = "2020-06-18T13:08:51.000+0000";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"created\": \"" + createdDate + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(json);
        assertNotNull(dr);
        assertNotNull(dr.getCreated());
    }

    private DirectReview parseJsonToDirectReview(String json) {
        ObjectMapper mapper = new ObjectMapper();
        DirectReview dr = null;
        JsonNode root = null;
        try {
            root = mapper.readTree(json);
        } catch (IOException ex) {
            fail("Could not convert " + json + " to JsonNode object.");
        }

        JsonNode issuesNode = root.get("issues");
        if (issuesNode.isArray()) {
            assertEquals(1, issuesNode.size());
            for (JsonNode issueNode : issuesNode) {
                try {
                    String jiraKey = issueNode.get("key").textValue();
                    JsonNode fields = issueNode.get("fields");
                    dr = mapper.readValue(fields.toString(), DirectReview.class);
                    dr.setJiraKey(jiraKey);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    fail("Cannot map issue JSON to DirectReview class");
                }
            }
        } else {
            fail("Issues node should be an array");
        }
        return dr;
    }
}
