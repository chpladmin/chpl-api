package gov.healthit.chpl.report.criteriauptodate;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdatedListingStatusReport {
    private LocalDate reportDay;
    private Long certifiedProductId;
    private String chplProductNumber;
    private String product;
    private String version;
    private String developer;
    private String certificationBody;
    private String certificationStatus;
    private Long developerId;
    private Long certificationBodyId;
    private Long certificationStatusId;
    private Integer totalUpdatesRequired;
}
