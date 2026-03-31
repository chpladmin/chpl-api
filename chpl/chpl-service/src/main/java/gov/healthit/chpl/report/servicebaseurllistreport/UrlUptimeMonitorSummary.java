package gov.healthit.chpl.report.servicebaseurllistreport;

import gov.healthit.chpl.domain.Developer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlUptimeMonitorSummary {
    private Developer developer;
    private String url;
    private Double percentPassed;
}
