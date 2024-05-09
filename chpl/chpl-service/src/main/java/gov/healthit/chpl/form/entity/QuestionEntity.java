package gov.healthit.chpl.form.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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
