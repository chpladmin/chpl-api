package gov.healthit.chpl.report.criteriamigrationreport;

import java.time.LocalDate;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CriteriaMigrationCount {
    private Long id;
    private CriteriaMigrationDefinition criteriaMigrationDefinition;
    private LocalDate reportDate;
    private Integer originalCriterionCount;
    private Integer updatedCriterionCount;
    private Integer originalToUpdatedCriterionCount;
}
