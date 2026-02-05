package gov.healthit.chpl.domain.compliance;

import java.util.ArrayList;
import java.util.List;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class CircumstancesDeserializer extends StdDeserializer<List<String>> {
    private static final String FIELD_NAME = "value";

    protected CircumstancesDeserializer() {
        super(List.class);
    }

    @Override
    public List<String> deserialize(JsonParser jsonParser, DeserializationContext context) {
        List<String> circumstanceValues = new ArrayList<String>();
        JsonNode circumstancesNode = jsonParser.objectReadContext().readTree(jsonParser);
        if (circumstancesNode != null && circumstancesNode.isArray() && circumstancesNode.size() > 0) {
            for (JsonNode circumstanceObj : circumstancesNode) {
                JsonNode circumstanceField = circumstanceObj.get(FIELD_NAME);
                if (circumstanceField != null) {
                    circumstanceValues.add(circumstanceField.asString());
                }
            }
        }
        return circumstanceValues;
    }
}
