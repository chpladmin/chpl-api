package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;

import gov.healthit.chpl.util.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrlUptimeMonitorTest {
    private Long id;
    private Long urlUptimeMonitorId;
    private String datadogTestKey;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private LocalDateTime checkTime;
    private Boolean passed;
}
