package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdatedCriteriaStatusReport {
    private Long id;
    private LocalDate reportDay;
    private Long certificationCriterionId;
    private Integer listingsWithCriterionCount;
    private Integer fullyUpToDateCount;
    private Integer functionalitiesTestedUpToDateCount;
    private Integer standardsUpToDateCount;
    private Integer codeSetsUpToDateCount;
}
