package gov.healthit.chpl.form.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.form.QuestionAllowedResponseMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question_allowed_response_map")
public class QuestionAllowedResponseMapEntity extends EntityAudit {
    private static final long serialVersionUID = 5285841792600768210L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, insertable = false, updatable = false)
    private QuestionEntity question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allowed_response_id", nullable = false, insertable = false, updatable = false)
    private AllowedResponseEntity allowedResponse;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public QuestionAllowedResponseMap toDomain() {
        return QuestionAllowedResponseMap.builder()
                .id(id)
                .question(question.toDomain())
                .response(allowedResponse.toDomain())
                .sortOrder(sortOrder)
                .build();
    }
}
