package gov.healthit.chpl.scheduler.job.urluptime;

import gov.healthit.chpl.domain.Developer;
import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class UrlUptimeMonitor {
    private Long id;
    private Developer developer;
    private String url;
    private String datadogPublicId;
}
