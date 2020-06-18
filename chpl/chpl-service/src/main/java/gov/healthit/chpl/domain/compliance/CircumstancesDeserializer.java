package gov.healthit.chpl.domain.compliance;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class CircumstancesDeserializer extends StdDeserializer<List<String>> {
    private static final long serialVersionUID = 1551090128532873867L;

    public CircumstancesDeserializer() {
        this(null);
    }

    public CircumstancesDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public List<String> deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException, JsonProcessingException {
        List<String> circumstanceValues = new ArrayList<String>();
        JsonNode circumstancesNode = jsonParser.getCodec().readTree(jsonParser);
        if (circumstancesNode != null && circumstancesNode.isArray() && circumstancesNode.size() > 0) {
            for (JsonNode circumstanceObj : circumstancesNode) {
                JsonNode circumstanceValue = circumstanceObj.get("value");
                if (circumstanceValue != null) {
                    circumstanceValues.add(circumstanceValue.textValue());
                }
            }
        }
        return circumstanceValues;
    }
}