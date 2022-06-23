package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.attestation.entity.DependentAttestationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DependentAttestation {
    private Long id;
    private Attestation attestation;
    private AttestationValidResponse whenParentValidResponse;
    private Long sortOrder;

    public DependentAttestation(DependentAttestationEntity entity) {
        this.id = entity.getId();
        this.attestation = new Attestation(entity.getAttestation());
        this.whenParentValidResponse = new AttestationValidResponse(entity.getWhenParentValidResponse());
        this.sortOrder = entity.getSortOrder();
    }
}
