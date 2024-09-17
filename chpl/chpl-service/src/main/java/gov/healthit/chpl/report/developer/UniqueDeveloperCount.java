package gov.healthit.chpl.report.developer;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UniqueDeveloperCount {
    private Long count;
}
