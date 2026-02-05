package gov.healthit.chpl.domain.compliance;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

@Log4j2
public class DateDeserializer extends StdDeserializer<LocalDate> {

    protected DateDeserializer() {
        super(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser jsonParser, DeserializationContext context) {
        LocalDate result = null;
        String dateStr = jsonParser.getValueAsString();
        if (!StringUtils.isEmpty(dateStr)) {
            try {
                result = LocalDate.parse(dateStr);
            } catch (DateTimeParseException ex) {
                LOGGER.error("Could not parse " + dateStr + " as a LocalDate.", ex);
            }
        }
        return result;
    }
}
