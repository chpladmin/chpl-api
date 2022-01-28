package gov.healthit.chpl.attestation.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

@Data
@Builder
public class AttestationForm {
    @Singular
    private List<Attestation> attestations;

    public AttestationForm(List<Attestation> attestations) {
        this.attestations = attestations;
    }
}
