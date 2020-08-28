package gov.healthit.chpl.domain.compliance;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

public class SimpleValueDeserializer extends JsonDeserializer<String> {
    private static final String FIELD_NAME = "value";

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException, JsonProcessingException {
        String value = "";
        JsonNode valueNode = jsonParser.getCodec().readTree(jsonParser);
        if (valueNode != null && valueNode.has(FIELD_NAME)) {
            value = valueNode.get(FIELD_NAME).textValue();
        }
        return value;
    }
}
