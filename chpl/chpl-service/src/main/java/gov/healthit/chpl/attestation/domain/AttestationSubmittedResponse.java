package gov.healthit.chpl.attestation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationSubmittedResponse {
    private Long id;
    private Attestation attestation;
    private AttestationValidResponse response;
}
