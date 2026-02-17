package gov.healthit.chpl.domain.compliance;

import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

@Log4j2
public class DeveloperIdDeserializer extends StdDeserializer<Long> {

    protected DeveloperIdDeserializer() {
        super(Long.class);
    }

    @Override
    public Long deserialize(JsonParser jsonParser, DeserializationContext context) {
        Long result = null;
        JsonNode developerIdNode = jsonParser.objectReadContext().readTree(jsonParser);
        if (developerIdNode != null && !developerIdNode.isNumber()) {
            //when developer ID comes out of jira it looks like a string
            try {
                result = Long.parseLong(developerIdNode.asString());
            } catch (NumberFormatException ex) {
                LOGGER.error("Could not parse " + developerIdNode.asString() + " as a developer ID (Long).");
            }
        } else if (developerIdNode != null && developerIdNode.isNumber()) {
            //when developer ID comes out of shared store it looks like a number
            result = developerIdNode.longValue();
        }
        return result;
    }
}
