package gov.healthit.chpl.directReview;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;

import org.junit.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import gov.healthit.chpl.domain.compliance.DirectReviewNonconformity;

public class DirectReviewNonconformityDeserializationTest {

    @Test
    public void deserializeJson_parsesRequirement() {
        String requirementValue = "170.404(b)(2)";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10933\": { "
                +    "\"self\": \"https://oncprojectracking.ahrqdev.org/support-jsd/rest/api/2/customFieldOption/10735\", "
                +    "\"value\": \"" + requirementValue + "\", "
                +    "\"id\": \"10735\" }"
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getRequirement());
        assertEquals(requirementValue, nc.getRequirement());
    }

    @Test
    public void deserializeJson_parsesDeveloperAssociatedListings() {
        String dalValue = "15.02.02.3007.A056.01.00.0.180214";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10943\": [\"" + dalValue + "\"] "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getDeveloperAssociatedListings());
        assertEquals(1, nc.getDeveloperAssociatedListings().size());
        assertEquals(dalValue, nc.getDeveloperAssociatedListings().get(0));
    }

    @Test
    public void deserializeJson_parsesNonconformityType() {
        String nonconformityType = "170.406(b)(1)";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10934\": { "
                +    "\"self\": \"https://oncprojectracking.ahrqdev.org/support-jsd/rest/api/2/customFieldOption/10735\", "
                +    "\"value\": \"" + nonconformityType + "\", "
                +    "\"id\": \"10735\" }"
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getNonconformityType());
        assertEquals(nonconformityType, nc.getNonconformityType());
    }

    @Test
    public void deserializeJson_parsesNonconformityStatus() {
        String nonconformityStatus = "Closed";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10935\": { "
                +    "\"self\": \"https://oncprojectracking.ahrqdev.org/support-jsd/rest/api/2/customFieldOption/10735\", "
                +    "\"value\": \"" + nonconformityStatus + "\", "
                +    "\"id\": \"10735\" }"
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getNonconformityStatus());
        assertEquals(nonconformityStatus, nc.getNonconformityStatus());
    }

    @Test
    public void deserializeJson_parsesNonconformitySummary() {
        String nonconformitySummary = "summary";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10927\": \"" + nonconformitySummary + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getNonconformitySummary());
        assertEquals(nonconformitySummary, nc.getNonconformitySummary());
    }

    @Test
    public void deserializeJson_parsesNonconformityFindings() {
        String nonconformityFindings = "findings";
        String json = "{"
                + "\"total\": 1,"
                + "\"issues\": ["
                + "{ "
                + "\"key\": \"DR-12345\", "
                + "\"fields\": {"
                + "\"customfield_10928\": \"" + nonconformityFindings + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getNonconformityFindings());
        assertEquals(nonconformityFindings, nc.getNonconformityFindings());
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
                + "\"customfield_10929\": \"" + devExplanation + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
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
                + "\"customfield_10930\": \"" + resolution + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
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
                + "\"customfield_10922\": \"" + date + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
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
                + "\"customfield_10923\": \"" + date + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
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
                + "\"customfield_10925\": \"" + date + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
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
                + "\"customfield_10926\": \"" + date + "\" "
                + "}"
                + "}"
                + "]"
                + "}";

        DirectReviewNonconformity nc = parseJsonToNonconformity(json);
        assertNotNull(nc);
        assertNotNull(nc.getCapEndDate());
    }

    private DirectReviewNonconformity parseJsonToNonconformity(String json) {
        ObjectMapper mapper = new ObjectMapper();
        DirectReviewNonconformity nonconformity = null;
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
                    nonconformity = mapper.readValue(fields, DirectReviewNonconformity.class);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    fail("Cannot map issue JSON to DirectReviewNonconformity class");
                }
            }
        } else {
            fail("Issues node should be an array");
        }
        return nonconformity;
    }
}
