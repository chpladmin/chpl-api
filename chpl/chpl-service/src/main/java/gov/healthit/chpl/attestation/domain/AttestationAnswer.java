package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.attestation.entity.AttestationAnswerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttestationAnswer {
    private Long id;
    private String answer;
    private String meaning;
    private Long sortOrder;

    public AttestationAnswer(AttestationAnswerEntity entity) {
        this.id = entity.getId();
        this.answer = entity.getAnswer();
        this.meaning = entity.getMeaning();
        this.sortOrder = entity.getSortOrder();
    }
}
