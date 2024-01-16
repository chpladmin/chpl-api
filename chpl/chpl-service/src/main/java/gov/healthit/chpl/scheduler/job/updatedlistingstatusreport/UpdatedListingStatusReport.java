package gov.healthit.chpl.scheduler.job.updatedlistingstatusreport;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdatedListingStatusReport {
    private Long id;
    private Long certifiedProductId;
    private LocalDate reportDay;
    private Long criteriaRequireUpdateCount;
    private Long daysUpdatedEarly;
    private String chplProductNumber;
    private String product;
    private String version;
    private String developer;
    private String certificationBody;
    private String certificationStatus;
    private Long developerId;
    private Long certificationBodyId;
    private Long certificationStatusId;

}
