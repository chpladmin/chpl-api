package gov.healthit.chpl.scheduler.job.summarystatistics.chart.domain;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveillanceActivitiesOverTime {
    private LocalDate date;
    private Long totalSurveillanceActivities;
    private Long totalOpenSurveillanceActivities;
    private Long totalClosedSurveillanceActivities;
}
