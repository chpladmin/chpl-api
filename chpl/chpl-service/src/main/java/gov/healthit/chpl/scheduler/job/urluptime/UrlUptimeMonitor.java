package gov.healthit.chpl.scheduler.job.urluptime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UrlUptimeMonitor {
    private Long id;
    private String description;
    private String url;
    private String datadogMonitorKey;
}
