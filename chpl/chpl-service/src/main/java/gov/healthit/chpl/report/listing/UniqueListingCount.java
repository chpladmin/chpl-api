package gov.healthit.chpl.report.listing;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniqueListingCount {
    private Long totalCount;
    private Long activeCount;
    private Long suspendedCount;
    private Long withdrawnCount;
}
