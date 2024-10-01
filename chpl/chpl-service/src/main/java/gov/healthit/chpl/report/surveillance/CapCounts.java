package gov.healthit.chpl.report.surveillance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CapCounts {
    private Long totalCaps;
    private Long openCaps;
    private Long closedCaps;
}
