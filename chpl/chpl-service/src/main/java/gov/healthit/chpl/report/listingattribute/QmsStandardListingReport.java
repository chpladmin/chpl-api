package gov.healthit.chpl.report.listingattribute;

import gov.healthit.chpl.qmsStandard.QmsStandard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QmsStandardListingReport {
    private String chplProductNumber;
    private String listingDetailsUrl;
    private QmsStandard qmsStandard;
}
