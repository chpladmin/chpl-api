package gov.healthit.chpl.attestation.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttestationForm {
    private List<AttestationQuestion> questions;

    public AttestationForm(List<AttestationQuestion> questions) {
        this.questions = questions;
    }
}
