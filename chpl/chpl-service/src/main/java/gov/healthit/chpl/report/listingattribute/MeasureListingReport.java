package gov.healthit.chpl.report.listingattribute;

import gov.healthit.chpl.listing.measure.domain.SimpleMeasure;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MeasureListingReport {
    private SimpleMeasure measure;
    private String chplProductNumber;
}
