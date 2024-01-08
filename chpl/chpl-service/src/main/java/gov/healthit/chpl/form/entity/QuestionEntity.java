package gov.healthit.chpl.form.entity;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.form.AllowedResponse;
import gov.healthit.chpl.form.Question;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Singular;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@ToString
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "question")
public class QuestionEntity extends EntityAudit {
    private static final long serialVersionUID = -2230649546856570735L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "response_cardinality_type_id", nullable = false, insertable = false, updatable = false)
    private ResponseCardinalityTypeEntity responseCardinalityType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "section_heading_id", nullable = false, insertable = false, updatable = false)
    private SectionHeadingEntity sectionHeading;

    @Column(name = "question")
    private String question;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "question")
    @Singular(value = "questionAllowedResponse")
    private List<QuestionAllowedResponseMapEntity> questionAllowedResponseMap;

    public Question toDomain() {
        return Question.builder()
                .id(id)
                .responseCardinalityType(responseCardinalityType.toDomain())
                .sectionHeading(sectionHeading != null ? sectionHeading.toDomain() : null)
                .allowedResponses(questionAllowedResponseMap.stream()
                        .map(ent -> {
                            AllowedResponse ar = ent.getAllowedResponse().toDomain();
                            ar.setSortOrder(ent.getSortOrder());
                            return ar;
                        })
                        .toList())
                .question(question)
                .build();

    }
}
