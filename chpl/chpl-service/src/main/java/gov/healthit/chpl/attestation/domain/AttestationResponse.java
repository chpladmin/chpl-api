package gov.healthit.chpl.attestation.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttestationResponse {
    private AttestationQuestion question;
    private AttestationResponse response;
}
