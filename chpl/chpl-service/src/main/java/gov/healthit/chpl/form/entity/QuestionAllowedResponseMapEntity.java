package gov.healthit.chpl.form.entity;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import gov.healthit.chpl.form.QuestionAllowedResponseMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "question_allowed_response_map")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionAllowedResponseMapEntity {
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

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    public QuestionAllowedResponseMap toDomain() {
        return QuestionAllowedResponseMap.builder()
                .id(id)
                .question(question.toDomain())
                .response(allowedResponse.toDomain())
                .sortOrder(sortOrder)
                .build();
    }
}
