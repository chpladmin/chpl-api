package gov.healthit.chpl.report.surveillance;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NonconformityCounts {
    private Long totalNonconformities;
    private Long openNonconformities;
    private Long closedNonconformities;
    private Long avgDaysToAssessConformity;
    private Long avgDaysToApproveCap;
    private Long avgDaysOfCap;
    private Long avgDaysFromCapApprovalToSurveillanceClose;
    private Long avgDaysFromCapCloseToSurveillanceClose;
    private Long avgDaysToCloseNonconformity;
}
