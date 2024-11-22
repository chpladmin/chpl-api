package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.domain.CertificationBody;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttestationReport {
    private Long id;
    private LocalDate reportDate;
    private CertificationBody certificationBody;
    private AttestationPeriod attestationPeriod;

    @Builder.Default
    private Long developerCount = 0L;

    @Builder.Default
    private Long approvedCount = 0L;

    @Builder.Default
    private Long pendingAcbActionCount = 0L;

    @Builder.Default
    private Long pendingDeveloperActionCount = 0L;

    @Builder.Default
    private Long noSubmissionCount = 0L;
}
