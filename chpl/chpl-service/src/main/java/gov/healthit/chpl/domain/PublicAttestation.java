package gov.healthit.chpl.domain;

import gov.healthit.chpl.attestation.domain.AttestationPeriod;
import gov.healthit.chpl.domain.concept.PublicAttestationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class PublicAttestation {
    private Long id;
    private AttestationPeriod attestationPeriod;
    private PublicAttestationStatus status;

    public String getStatusText() {
        return status.getName();
    }
}
