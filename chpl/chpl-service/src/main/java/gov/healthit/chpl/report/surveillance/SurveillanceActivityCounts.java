package gov.healthit.chpl.report.surveillance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SurveillanceActivityCounts {
    private Long totalActivities;
    private Long openActivities;
    private Long closedActivities;
    private Long averageDurationClosedSurveillance;
}
