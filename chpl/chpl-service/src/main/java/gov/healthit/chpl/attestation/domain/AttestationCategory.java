package gov.healthit.chpl.attestation.domain;

import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.attestation.entity.AttestationCategoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AttestationCategory {
    private Long id;
    private List<AttestationQuestion> questions;
    private String name;
    private Long sortOrder;

    public AttestationCategory(AttestationCategoryEntity entity) {
        this.id = entity.getId();
        this.questions = entity.getQuestions().stream()
                .map(ent -> new AttestationQuestion(ent))
                .collect(Collectors.toList());
        this.name = entity.getName();
        this.sortOrder = entity.getSortOrder();
    }
}
