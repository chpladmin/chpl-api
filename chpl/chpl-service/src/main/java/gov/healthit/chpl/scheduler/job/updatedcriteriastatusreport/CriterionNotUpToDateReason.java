package gov.healthit.chpl.scheduler.job.updatedcriteriastatusreport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class CriterionNotUpToDateReason {
    private Long id;
    private String name;
}
