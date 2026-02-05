package gov.healthit.chpl.domain.compliance;

import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

public class SimpleValueDeserializer extends StdDeserializer<String> {
    private static final String FIELD_NAME = "value";

    protected SimpleValueDeserializer() {
        super(String.class);
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext context) {
        String value = "";
        JsonNode valueNode = jsonParser.objectReadContext().readTree(jsonParser);
        if (valueNode != null && valueNode.has(FIELD_NAME)) {
            value = valueNode.get(FIELD_NAME).asString();
        } else if (valueNode != null && valueNode.isString()) {
            //when value node comes out of shared store it looks like a string
            value = valueNode.asString();
        }
        return value;
    }
}
