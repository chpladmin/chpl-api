package gov.healthit.chpl.report.listingattribute;

import gov.healthit.chpl.accessibilityStandard.AccessibilityStandard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessibilityStandardListingReport {
    private String chplProductNumber;
    private String listingDetailsUrl;
    private AccessibilityStandard accessibilityStandard;
}
