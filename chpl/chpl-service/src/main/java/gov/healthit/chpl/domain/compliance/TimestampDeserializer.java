package gov.healthit.chpl.domain.compliance;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class TimestampDeserializer extends StdDeserializer<Date> {
    private static final long serialVersionUID = 1551095367912873867L;
    private SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    public TimestampDeserializer() {
        this(null);
    }

    public TimestampDeserializer(Class<?> vc) {
        super(vc);
    }

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
