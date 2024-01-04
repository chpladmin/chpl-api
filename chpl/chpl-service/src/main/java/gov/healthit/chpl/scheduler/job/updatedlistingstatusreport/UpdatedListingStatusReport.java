package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdatedListingStatusReport {
    private Long id;
    private Long certifiedProductId;
    private Integer criteriaRequireUpdateCount;
    private Integer daysUpdatedEarly;
}
