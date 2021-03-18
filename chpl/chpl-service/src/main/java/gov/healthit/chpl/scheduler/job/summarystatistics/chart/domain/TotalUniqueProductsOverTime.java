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
public class TotalUniqueProductsOverTime {
    private LocalDate date;
    private Long totalProductsListings;
}
