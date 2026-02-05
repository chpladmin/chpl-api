package gov.healthit.chpl.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class SystemToEasternLocalDateTimeReportSerializer extends StdSerializer<LocalDateTime> {
    private static final long serialVersionUID = 4988689694731636895L;
    private DateTimeFormatter formatter;

    public SystemToEasternLocalDateTimeReportSerializer() {
        super(LocalDateTime.class);
        this.formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:mm a");
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializationContext sc) {
        LocalDateTime inEastern = DateUtil.fromSystemToEastern(value);
        gen.writeString(inEastern.format(formatter));
    }
}
