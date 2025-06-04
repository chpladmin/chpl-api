package gov.healthit.chpl.report.listingattribute;

import gov.healthit.chpl.qmsStandard.QmsStandard;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class QmsStandardReport {
    private QmsStandard qmsStandard;
    private Long count;
}
