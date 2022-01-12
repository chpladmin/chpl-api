package gov.healthit.chpl.attestation.domain;

import java.util.List;
import java.util.stream.Collectors;

import gov.healthit.chpl.attestation.entity.AttestationQuestionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class AttestationQuestion {
    private Long id;
    private List<AttestationAnswer> answers;
    private String question;
    private Long sortOrder;

    public AttestationQuestion(AttestationQuestionEntity entity) {
        this.id = entity.getId();
        this.answers = entity.getAnswers().stream()
                .map(ent -> new AttestationAnswer(ent))
                .collect(Collectors.toList());
        this.question = entity.getQuestion();
        this.sortOrder = entity.getSortOrder();
    }
}
