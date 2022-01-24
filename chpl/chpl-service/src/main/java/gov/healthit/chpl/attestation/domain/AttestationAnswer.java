package gov.healthit.chpl.attestation.domain;

import gov.healthit.chpl.attestation.entity.AttestationAnswerEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
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
