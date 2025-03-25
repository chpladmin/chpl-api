package gov.healthit.chpl.scheduler.job.report.attestation;

import gov.healthit.chpl.changerequest.domain.ChangeRequestStatusType;
import gov.healthit.chpl.domain.Developer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttestationReportDeveloper {
    private Long id;
    private AttestationReport attestationReport;
    private Developer developer;
    private ChangeRequestStatusType changeRequestStatusType;
}
