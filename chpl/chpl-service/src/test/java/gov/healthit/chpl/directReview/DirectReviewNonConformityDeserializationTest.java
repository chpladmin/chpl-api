package gov.healthit.chpl.directReview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.compliance.DirectReviewNonConformity;

public class DirectReviewNonConformityDeserializationTest {

    @Test
    public void deserializeJson_parsesRequirement() {
        String requirementValue = "170.404(b)(2)";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11018\": { "
                +    "\"self\": \"https://oncprojectracking.ahrqdev.org/support-jsd/rest/api/2/customFieldOption/10735\", "
                +    "\"value\": \"" + requirementValue + "\", "
                +    "\"id\": \"10735\" }"
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getRequirement());
        assertEquals(requirementValue, nc.getRequirement());
    }

    @Test
    public void deserializeJson_parsesNonConformityType() {
        String nonConformityType = "170.406(b)(1)";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11036\": \"" + nonConformityType + "\""
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getNonConformityType());
        assertEquals(nonConformityType, nc.getNonConformityType());
    }

    @Test
    public void deserializeJson_parsesNonConformityStatus() {
        String nonConformityStatus = "Closed";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11035\": \"Closed\""
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getNonConformityStatus());
        assertEquals(nonConformityStatus, nc.getNonConformityStatus());
    }

    @Test
    public void deserializeJson_parsesDateOfDetermination() {
        String dateOfDetermination = "2020-06-24";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11021\": \"" + dateOfDetermination + "\""
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getDateOfDetermination());
    }

    @Test
    public void deserializeJson_parsesNonConformitySummary() {
        String nonConformitySummary = "summary";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11026\": \"" + nonConformitySummary + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getNonConformitySummary());
        assertEquals(nonConformitySummary, nc.getNonConformitySummary());
    }

    @Test
    public void deserializeJson_parsesNonConformityFindings() {
        String nonConformityFindings = "findings";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11027\": \"" + nonConformityFindings + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getNonConformityFindings());
        assertEquals(nonConformityFindings, nc.getNonConformityFindings());
    }

    @Test
    public void deserializeJson_parsesDeveloperExplanation() {
        String devExplanation = "explanation";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11028\": \"" + devExplanation + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getDeveloperExplanation());
        assertEquals(devExplanation, nc.getDeveloperExplanation());
    }

    @Test
    public void deserializeJson_parsesResolution() {
        String resolution = "resolution";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11029\": \"" + resolution + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getResolution());
        assertEquals(resolution, nc.getResolution());
    }

    @Test
    public void deserializeJson_parsesCapApprovalDate() {
        String date = "2020-06-25";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11022\": \"" + date + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getCapApprovalDate());
    }

    @Test
    public void deserializeJson_parsesCapStartDate() {
        String date = "2020-06-25";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11023\": \"" + date + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getCapStartDate());
    }

    @Test
    public void deserializeJson_parsesCapMustCompleteDate() {
        String date = "2020-06-25";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11024\": \"" + date + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getCapMustCompleteDate());
    }

    @Test
    public void deserializeJson_parsesCapEndDate() {
        String date = "2020-06-25";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_11025\": \"" + date + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonConformity nc = parseJsonToNonConformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getCapEndDate());
    }

    private DirectReviewNonConformity parseJsonToNonConformity(String json) {
        ObjectMapper mapper = new ObjectMapper();
        DirectReviewNonConformity nonConformity = null;
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
                    nonConformity = mapper.readValue(fields, DirectReviewNonConformity.class);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    fail("Cannot map issue JSON to DirectReviewNonConformity class");
                }
            }
        } else {
            fail("Issues node should be an array");
        }
        return nonConformity;
    }
}
