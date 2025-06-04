package gov.healthit.chpl.report.listingattribute;

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessibilityStandardReport {
    private AccessibilityStandard accessibilityStandard;
    private Long count;
}
