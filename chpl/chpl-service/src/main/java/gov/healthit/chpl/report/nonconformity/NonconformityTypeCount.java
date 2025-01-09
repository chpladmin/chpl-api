package gov.healthit.chpl.report.nonconformity;

import gov.healthit.chpl.domain.NonconformityType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NonconformityTypeCount {
    private NonconformityType nonconformityType;
    private Long count;
}
