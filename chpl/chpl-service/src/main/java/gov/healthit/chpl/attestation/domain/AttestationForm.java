package gov.healthit.chpl.attestation.domain;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttestationForm {
    private List<AttestationCategory> categories;

    public AttestationForm(List<AttestationCategory> categories) {
        this.categories = categories;
    }
}
