package gov.healthit.chpl.scheduler.job.urluptime;

import gov.healthit.chpl.domain.Developer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DatadogMonitor {
    private Long id;
    private Developer developer;
    private String url;
    private String datadogPublicId;
}
