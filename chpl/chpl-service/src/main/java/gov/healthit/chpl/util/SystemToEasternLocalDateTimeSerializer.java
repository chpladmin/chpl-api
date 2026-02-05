package gov.healthit.chpl.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class SystemToEasternLocalDateTimeSerializer extends StdSerializer<LocalDateTime> {

    private static final long serialVersionUID = 1L;

    public SystemToEasternLocalDateTimeSerializer() {
        super(LocalDateTime.class);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext sc) {
        LocalDateTime inEastern = DateUtil.fromSystemToEastern(value);
        gen.writeString(inEastern.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }
}
