package gov.healthit.chpl.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class EasternToUtcLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    protected EasternToUtcLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }


    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        try {
            LocalDateTime easternInput = LocalDateTime.parse(jp.readValueAs(String.class));
            return DateUtil.fromEasternToUtc(easternInput);
        } catch (DateTimeParseException e) {
            LOGGER.info(e.getMessage(), e);
            return null;
        }
    }
}
