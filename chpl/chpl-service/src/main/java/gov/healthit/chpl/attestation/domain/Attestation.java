package gov.healthit.chpl.attestation.domain;

import java.util.List;
import java.util.stream.Collectors;

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
public class Attestation {
    private Long id;
    @Singular
    private List<AttestationValidResponse> validResponses;
    private Condition condition;
    private String description;
    private Long sortOrder;

    public Attestation(AttestationEntity entity) {
        this.id = entity.getId();
        this.validResponses = entity.getValidResponses().stream()
                .map(ent -> new AttestationValidResponse(ent))
                .collect(Collectors.toList());
        this.condition = new Condition(entity.getCondition());
        this.description = entity.getDescription();
        this.sortOrder = entity.getSortOrder();
    }
}
