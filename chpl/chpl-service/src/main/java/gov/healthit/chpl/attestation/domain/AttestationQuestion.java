package gov.healthit.chpl.attestation.domain;

import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.attestation.entity.AttestationQuestionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationQuestion {
    private Long id;
    private List<AttestationAnswer> answers;
    private AttestationCategory category;
    private String question;
    private Long sortOrder;

    public AttestationQuestion(AttestationQuestionEntity entity) {
        this.id = entity.getId();
        this.answers = entity.getAnswers().stream()
                .map(ent -> new AttestationAnswer(ent))
                .collect(Collectors.toList());
        this.category = new AttestationCategory(entity.getCategory());
        this.question = entity.getQuestion();
        this.sortOrder = entity.getSortOrder();
    }
}
