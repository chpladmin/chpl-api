package gov.healthit.chpl.attestation.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationResponse {
    private Long id;
    private AttestationQuestion question;
    private AttestationAnswer answer;
}
