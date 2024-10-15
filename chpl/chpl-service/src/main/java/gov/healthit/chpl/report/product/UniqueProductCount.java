package gov.healthit.chpl.report.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniqueProductCount {
    private Long totalCount;
    private Long activeCount;
    private Long suspendedCount;
    private Long withdrawnCount;
}
