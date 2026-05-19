package gov.healthit.chpl.report.criteriauptodate;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class UpdatedDeveloperStatusReport {
    private LocalDate reportDay;
    private Long developerId;
    private String developerName;
    private String developerDetailsUrl;
    private Integer totalListingsRequiringUpdate;
    private Integer totalListingsUpToDate;
}
