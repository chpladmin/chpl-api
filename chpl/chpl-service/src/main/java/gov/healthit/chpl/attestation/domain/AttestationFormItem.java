package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.attestation.entity.AttestationFormItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AttestationFormItem {
    private Long id;
    private AttestationPeriod attestationPeriod;
    private Attestation attestation;
    private Long sortOrder;
    private Boolean required;

    public AttestationFormItem(AttestationFormItemEntity entity) {
        this.id = entity.getId();
        this.attestationPeriod = new AttestationPeriod(entity.getAttestationPeriod());
        this.attestation = new Attestation(entity.getAttestation());
        this.sortOrder = entity.getSortOrder();
        this.required = entity.getRequired();
    }
}
