package gov.healthit.chpl.attestation.domain;

import java.util.List;

import gov.healthit.chpl.attestation.entity.AttestationFormItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AttestationFormItem {
    private Long id;
    //private AttestationPeriod attestationPeriod;
    private Attestation attestation;
    private List<AttestationFormItem> childAttestationFormItems;
    private ValidResponse whenValidResponse;
    private Long sortOrder;
    private Boolean required;

    public AttestationFormItem(AttestationFormItemEntity entity) {
        this.id = entity.getId();
        //this.attestationPeriod = new AttestationPeriod(entity.getAttestationPeriod());
        this.attestation = new Attestation(entity.getAttestation());
        if (entity.getWhenValidResponse() != null) {
            this.whenValidResponse = new ValidResponse(entity.getWhenValidResponse());
        }
        this.sortOrder = entity.getSortOrder();
        this.required = entity.getRequired();
    }
}
