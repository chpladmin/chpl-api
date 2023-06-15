package gov.healthit.chpl.scheduler.job.urluptime;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatadogMonitorTest {
    private Long id;
    private Long datadogMonitorId;
    private String datadogTestKey;
    private LocalDateTime checkTime;
    private Boolean passed;
}
