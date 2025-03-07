package gov.healthit.chpl.report.servicebaseurllistreport;

import java.util.List;

import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitor;
import gov.healthit.chpl.scheduler.job.urluptime.UrlUptimeMonitorTest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class UrlUptimeMonitorEx extends UrlUptimeMonitor {
    private List<UrlUptimeMonitorTest> tests;
    // private List<IdNamePair> acbs;
}
