package gov.healthit.chpl.attestation.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Attestation {
    private AttestationPeriod period;
    private List<AttestationResponse> responses;
}
