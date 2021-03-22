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
public class ListingsOverTime {
    private LocalDate date;
    private Long totalListings;
    private Long total2011Listings;
    private Long total2014Listings;
    private Long total2015Listings;
}
