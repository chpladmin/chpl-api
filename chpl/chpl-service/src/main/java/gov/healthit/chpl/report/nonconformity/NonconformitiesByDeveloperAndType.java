package gov.healthit.chpl.report.nonconformity;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NonconformitiesByDeveloperAndType {
    private int id;
    private boolean nonconformityClosed;
    private String nonconformityTypeName;
    private String nonconformityClassification;
    private Long developerId;
    private String developerName;
    private Long listingId;
    private String chplProductNumber;
}
