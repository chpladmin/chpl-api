package gov.healthit.chpl.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import lombok.extern.log4j.Log4j2;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.deser.std.StdDeserializer;

@Log4j2
public class EasternToSystemLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    protected EasternToSystemLocalDateTimeDeserializer() {
        super(LocalDateTime.class);
    }

    @Override
    public LocalDateTime deserialize(JsonParser jp, DeserializationContext ctxt) {
        try {
            LocalDateTime easternInput = LocalDateTime.parse(jp.readValueAs(String.class));
            return DateUtil.fromEasternToSystem(easternInput);
        } catch (DateTimeParseException e) {
            LOGGER.info(e.getMessage(), e);
            return null;
        }
    }
}
