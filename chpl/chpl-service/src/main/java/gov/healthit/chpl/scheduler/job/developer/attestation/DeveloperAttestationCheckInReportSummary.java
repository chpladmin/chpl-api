package gov.healthit.chpl.scheduler.job.developer.attestation;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeveloperAttestationCheckInReportSummary {
    private Long developerCount;
    private Long attestationsApprovedCount;
    private Long pendingAcbActionCount;
    private Long pendingDeveloperActionCount;
    private Long noSubmissionCount;
}
