package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ListingNotUpToDateReason {
    private Long id;
    private String name;
}
