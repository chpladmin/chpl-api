package gov.healthit.chpl.scheduler.job.developer.attestation;

import lombok.Data;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
public class CheckInReportSummary {
    private Long developerCount;
    private Long attestationsApprovedCount;
    private Long pendingAcbActionCount;
    private Long pendingDeveloperActionCount;
    private Long noSubmissionCount;

    public Boolean doCountsEqualDeveloperCount() {
        return Long.valueOf(attestationsApprovedCount + pendingAcbActionCount + pendingDeveloperActionCount + noSubmissionCount).equals(developerCount);
    }
}
