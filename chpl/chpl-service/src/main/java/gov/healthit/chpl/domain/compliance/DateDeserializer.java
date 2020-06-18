package gov.healthit.chpl.domain.compliance;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class DateDeserializer extends StdDeserializer<LocalDate> {
    private static final long serialVersionUID = 1551090127912873867L;

    public DateDeserializer() {
        this(null);
    }

    public DateDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context)
      throws IOException, JsonProcessingException {
        LocalDate result = null;
        JsonNode dateNode = jsonParser.getCodec().readTree(jsonParser);
        if (dateNode != null && !StringUtils.isEmpty(dateNode.textValue())) {
            try {
                result = LocalDate.parse(dateNode.textValue());
            } catch (DateTimeParseException ex) {
                LOGGER.error("Could not parse " + dateNode.textValue() + " as a LocalDate.", ex);
            }
        }
        return result;
    }
}