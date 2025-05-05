package gov.healthit.chpl.util;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class SystemToEasternLocalDateTimeReportSerializer extends StdSerializer<LocalDateTime> {
    private static final long serialVersionUID = 4988689694731636895L;
    private DateTimeFormatter formatter;

    public SystemToEasternLocalDateTimeReportSerializer() {
        super(LocalDateTime.class);
        this.formatter = DateTimeFormatter.ofPattern("M/d/yyyy h:m a");
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider sp)
            throws IOException, JsonProcessingException {
        LocalDateTime inEastern = DateUtil.fromSystemToEastern(value);
        gen.writeString(inEastern.format(formatter));
    }
}
