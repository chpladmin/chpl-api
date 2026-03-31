package gov.healthit.chpl.report.servicebaseurllistreport;

import java.util.List;

import gov.healthit.chpl.domain.Developer;
import gov.healthit.chpl.domain.IdNamePair;
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
    private List<IdNamePair> acbs;
}
