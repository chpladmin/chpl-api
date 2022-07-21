package gov.healthit.chpl.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class LocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    protected LocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }


    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        String localDateTimeString = jp.readValueAs(String.class);
        if (StringUtils.isEmpty(localDateTimeString)) {
            return null;
        }

        try {
            return LocalDateTime.parse(localDateTimeString);
        } catch (DateTimeParseException e) {
            LOGGER.info(e.getMessage(), e);
            return null;
        }
    }
}
