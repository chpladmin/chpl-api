package gov.healthit.chpl.domain.compliance;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TimestampDeserializer extends JsonDeserializer<Date> {
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Override
    public Date deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException, JsonProcessingException {
        Date result = null;
        JsonNode timestampNode = jsonParser.getCodec().readTree(jsonParser);
        if (timestampNode != null && !StringUtils.isEmpty(timestampNode.textValue())) {
            try {
                result = formatter.parse(timestampNode.textValue());
            } catch (ParseException ex) {
                LOGGER.error("Could not parse " + timestampNode.textValue() + " as a Date.", ex);
            }
        }
        return result;
    }
}
