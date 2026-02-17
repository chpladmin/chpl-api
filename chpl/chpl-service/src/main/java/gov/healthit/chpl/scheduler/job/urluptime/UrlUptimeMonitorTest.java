package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDateTime;

import gov.healthit.chpl.util.LocalDateTimeDeserializer;
import gov.healthit.chpl.util.LocalDateTimeSerializer;
import lombok.Builder;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

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
