package gov.healthit.chpl.domain.compliance;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.deser.std.StdDeserializer;

@Log4j2
public class TimestampDeserializer extends StdDeserializer<Date> {
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");

    protected TimestampDeserializer() {
        super(Date.class);
    }

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext context) {
        Date result = null;
        JsonNode timestampNode = jsonParser.objectReadContext().readTree(jsonParser);
        if (timestampNode != null && !timestampNode.isNumber()
                && !StringUtils.isEmpty(timestampNode.asString())) {
            //when timestamp comes out of jira it looks like a date/time formatted string
            try {
                result = formatter.parse(timestampNode.asString());
            } catch (ParseException ex) {
                LOGGER.error("Could not parse " + timestampNode.asString() + " as a Date.", ex);
            }
        } else if (timestampNode != null && timestampNode.isNumber()) {
            //when timestamp node comes out of shared store it looks like a millis-long value
            long timestampMillis = timestampNode.longValue();
            result = new Date(timestampMillis);
        }
        return result;
    }
}
