package gov.healthit.chpl.domain.compliance;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DeveloperIdDeserializer extends JsonDeserializer<Long> {

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException, JsonProcessingException {
        Long result = null;
        JsonNode developerIdNode = jsonParser.getCodec().readTree(jsonParser);
        if (developerIdNode != null && !developerIdNode.isNumber()) {
            //when developer ID comes out of jira it looks like a string
            try {
                result = Long.parseLong(developerIdNode.textValue());
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not parse " + developerIdNode.textValue() + " as a developer ID (Long).");
            }
        } else if (developerIdNode != null && developerIdNode.isNumber()) {
            //when developer ID comes out of shared store it looks like a number
            result = developerIdNode.longValue();
        }
        return result;
    }
}
