package gov.healthit.chpl.web.controller.results;

import java.io.Serializable;
import java.util.List;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.attestation.domain.DeveloperAttestationSubmission;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DeveloperAttestationSubmissionResults implements Serializable {
    private static final long serialVersionUID = -3345114923797354483L;

    private List<DeveloperAttestationSubmission> developerAttestations;

    @Deprecated
    private Boolean canSubmitAttestationChangeRequest = false;

    private AttestationPeriod submittablePeriod;
    private Boolean canCreateException = false;
}
