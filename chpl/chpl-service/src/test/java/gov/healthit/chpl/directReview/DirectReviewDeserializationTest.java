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
    public void serializeJson_parsesChplNumber() {
        String chplProductNumber = "15.04.04.2858.Tsys.06.01.1.191223";
        String externalJson = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"fields\": {"
                + "\"key\": \"DR-12345\", "
                + "\"customfield_10213\": \"" + chplProductNumber + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(externalJson);
        assertNotNull(dr);
        assertNotNull(dr.getChplProductNumber());
        assertEquals(chplProductNumber, dr.getChplProductNumber());
    }

    @Test
    public void serializeJson_parsesStartDate() {
        String startDate = "2020-06-01";
        String externalJson = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"fields\": {"
                + "\"key\": \"DR-12345\", "
                + "\"customfield_10919\": \"" + startDate + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(externalJson);
        assertNotNull(dr);
        assertNotNull(dr.getStartDate());
        assertEquals(startDate, dr.getStartDate().toString());
    }

    @Test
    public void serializeJson_parsesEndDate() {
        String endDate = "2020-06-30";
        String externalJson = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"fields\": {"
                + "\"key\": \"DR-12345\", "
                + "\"customfield_10920\": \"" + endDate + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(externalJson);
        assertNotNull(dr);
        assertNotNull(dr.getEndDate());
        assertEquals(endDate, dr.getEndDate().toString());
    }

    @Test
    public void serializeJson_parsesCircumstances() {
        String circumstanceName = "Noncompliance to Conditions and Maintenance of Certification";
        String externalJson = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"fields\": {"
                + "\"key\": \"DR-12345\", "
                + "\"customfield_10932\": [{"
                + "  \"self\": \"https://oncprojectracking.ahrqdev.org/support-jsd/rest/api/2/customFieldOption/10718\","
                + "  \"value\": \"" + circumstanceName + "\","
                + "  \"id\": \"10718\""
                + "}]"
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(externalJson);
        assertNotNull(dr);
        assertNotNull(dr.getCircumstances());
        assertEquals(1, dr.getCircumstances().size());
        assertEquals(circumstanceName, dr.getCircumstances().get(0));
    }

    @Test
    public void serializeJson_parsesLastUpdatedDate() {
        String lastUpdatedDate = "2020-06-18T13:08:51.000+0000";
        String externalJson = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"fields\": {"
                + "\"key\": \"DR-12345\", "
                + "\"updated\": \"" + lastUpdatedDate + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(externalJson);
        assertNotNull(dr);
        assertNotNull(dr.getLastUpdated());
    }

    @Test
    public void serializeJson_parsesCreatedDate() {
        String createdDate = "2020-06-18T13:08:51.000+0000";
        String externalJson = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"fields\": {"
                + "\"key\": \"DR-12345\", "
                + "\"created\": \"" + createdDate + "\" }"
                + "}"
                + "]"
                + "}";

        DirectReview dr = parseJsonToDirectReview(externalJson);
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
                    String fields = issueNode.get("fields").toString();
                    dr = mapper.readValue(fields, DirectReview.class);
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
