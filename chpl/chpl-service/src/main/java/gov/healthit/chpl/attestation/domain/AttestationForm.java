package gov.healthit.chpl.attestation.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttestationForm {
    private List<Attestation> attestations;

    public AttestationForm(List<Attestation> attestations) {
        this.attestations = attestations;
    }
}
