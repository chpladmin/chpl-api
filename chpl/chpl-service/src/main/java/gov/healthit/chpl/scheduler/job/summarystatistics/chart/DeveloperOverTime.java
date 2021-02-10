package gov.healthit.chpl.scheduler.job.summarystatistics.chart;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeveloperOverTime {
    private LocalDate date;
    private Long totalDevelopers;
    private Long totalDevelopersWith2014Listings;
    private Long totalDevelopersWith2015Listings;
}
