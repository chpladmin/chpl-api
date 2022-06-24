package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.attestation.entity.DependentAttestationFormItemEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DependentAttestation {
    private Long id;
    private ValidResponse whenValidResponse;
    private Attestation childAttestation;
    private Long sortOrder;

    public DependentAttestation(DependentAttestationFormItemEntity entity) {
        this.id = entity.getId();
        this.whenValidResponse = new ValidResponse(entity.getWhenValidResponse());
        this.childAttestation = new Attestation(entity.getChildAttestation());
        this.sortOrder = entity.getSortOrder();
    }
}
