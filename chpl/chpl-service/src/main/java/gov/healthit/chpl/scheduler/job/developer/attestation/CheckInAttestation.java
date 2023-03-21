package gov.healthit.chpl.scheduler.job.developer.attestation;

import gov.healthit.chpl.attestation.domain.AttestationSubmission;
import gov.healthit.chpl.changerequest.domain.ChangeRequest;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckInAttestation {
    private AttestationSubmission attestationSubmission;
    private ChangeRequest changeRequest;
    // private CheckInReportSource source;
}
