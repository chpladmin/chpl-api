package gov.healthit.chpl.domain.compliance;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class SimpleValueDeserializer extends StdDeserializer<String> {
    private static final long serialVersionUID = 1551090128532873867L;
    private static final String FIELD_NAME = "value";

    public SimpleValueDeserializer() {
        this(null);
    }

    public SimpleValueDeserializer(Class<?> vc) {
        super(vc);
    }

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