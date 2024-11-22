package gov.healthit.chpl.scheduler.job.report.attestation;

import java.time.LocalDate;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.domain.CertificationBody;
import gov.healthit.chpl.scheduler.job.developer.attestation.CheckInReportSummary;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@EqualsAndHashCode(callSuper = false)
@SuperBuilder
public class AttestationReport extends CheckInReportSummary {
    private Long id;
    private LocalDate reportDate;
    private CertificationBody certificationBody;
    private AttestationPeriod attestationPeriod;
}
