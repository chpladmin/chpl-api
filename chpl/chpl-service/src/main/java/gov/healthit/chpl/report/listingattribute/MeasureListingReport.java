package gov.healthit.chpl.report.listingattribute;

import gov.healthit.chpl.domain.Measure;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MeasureListingReport {
    private Measure measure;
    private String chplProductNumber;
}
