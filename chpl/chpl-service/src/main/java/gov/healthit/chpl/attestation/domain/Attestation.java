package gov.healthit.chpl.attestation.domain;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import gov.healthit.chpl.attestation.entity.AttestationEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Attestation {
    private Long id;

    @Singular
    private List<ValidResponse> validResponses;

    @Singular
    private List<DependentAttestation> dependentAttestations;

    private Condition condition;
    private String description;
    private Long sortOrder;

    public Attestation(AttestationEntity entity) {
        this.id = entity.getId();
        this.validResponses = entity.getValidResponses().stream()
                .map(ent -> new ValidResponse(ent))
                .toList();
        if (entity.getCondition() != null) {
            this.condition = new Condition(entity.getCondition());
        }
        this.description = entity.getDescription();
        this.sortOrder = entity.getSortOrder();
    }
}
