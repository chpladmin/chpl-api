package gov.healthit.chpl.util;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;

import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

@Log4j2
public class LocalDateDeserializer extends StdDeserializer<LocalDate> {

    private static final long serialVersionUID = 1L;

    protected LocalDateDeserializer() {
        super(LocalDate.class);
    }


    @Override
    public LocalDate deserialize(JsonParser jp, DeserializationContext ctxt) {
        String localDateString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(localDateString)) {
            return null;
        }

        try {
            return LocalDate.parse(localDateString);
        } catch (DateTimeParseException e) {
            LOGGER.info(e.getMessage(), e);
            return null;
        }
    }
}
