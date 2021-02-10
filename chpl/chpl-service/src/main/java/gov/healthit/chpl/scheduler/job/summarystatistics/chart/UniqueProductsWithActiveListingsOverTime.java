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
public class UniqueProductsWithActiveListingsOverTime {
    private LocalDate date;
    private Long totalProductsWithActiveListings;
    private Long totalProductsWithActive2014Listings;
    private Long totalProductsWithActive2015Listings;
}
