package gov.healthit.chpl.form.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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
