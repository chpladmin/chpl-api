package gov.healthit.chpl.attestation.domain;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
public class AttestationForm {
    @Singular
    private List<Attestation> attestations;
    private AttestationPeriod period;
}
