package gov.healthit.chpl.scheduler.job.urluptime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UrlUptimeMonitorTestCounts {

    private Long urlUptimeMonitorId;
    @Builder.Default
    private Long totalTestCount = 0L;
    @Builder.Default
    private Long totalSuccessfulTestCount = 0L;
    @Builder.Default
    private Long currentMonthTestCount = 0L;
    @Builder.Default
    private Long currentMonthSuccessfulTestCount = 0L;
    @Builder.Default
    private Long pastWeekTestCount = 0L;
    @Builder.Default
    private Long pastWeekSuccessfulTestCount = 0L;
}
