package gov.healthit.chpl.domain.compliance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class CircumstancesDeserializer extends JsonDeserializer<List<String>> {
    private static final String FIELD_NAME = "value";

    @Override
    public List<String> deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException, JsonProcessingException {
        List<String> circumstanceValues = new ArrayList<String>();
        JsonNode circumstancesNode = jsonParser.getCodec().readTree(jsonParser);
        if (circumstancesNode != null && circumstancesNode.isArray() && circumstancesNode.size() > 0) {
            for (JsonNode circumstanceObj : circumstancesNode) {
                JsonNode circumstanceField = circumstanceObj.get(FIELD_NAME);
                if (circumstanceField != null) {
                    circumstanceValues.add(circumstanceField.textValue());
                }
            }
        }
        return circumstanceValues;
    }
}
