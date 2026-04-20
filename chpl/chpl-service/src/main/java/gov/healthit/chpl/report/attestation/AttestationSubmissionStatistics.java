package gov.healthit.chpl.report.attestation;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AttestationSubmissionStatistics {

    private boolean isSubmissionWindowOpen;
    //The approval window is the length of time CHPL gathers nightly counts of
    //attestation submission statistics. It includes the submission window but generally
    //continues a bit longer for account for late submissions.
    private boolean isApprovalWindowOpen;
    //useful in the Power BI visual to indicate the maximum remaining devs to submit
    private Integer maxDevelopersWithoutAttestationsCount;
    //useful in the Power BI visual to indicate the minimum remaining devs to submit
    private Integer minDevelopersWithoutAttestationsCount = 0;
    private Integer developersWithoutSubmissionsCount;
    private Integer developersWithPendingSubmissionsCount;
}
