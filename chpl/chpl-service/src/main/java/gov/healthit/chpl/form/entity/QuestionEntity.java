package gov.healthit.chpl.form.entity;

import java.util.Date;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import gov.healthit.chpl.form.Question;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "question")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuestionEntity {

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

    @OneToMany
    @JoinTable(name = "question_allowed_response_map",
            joinColumns = @JoinColumn(name = "question_id"),
            inverseJoinColumns = @JoinColumn(name = "allowed_response_id"))
    private Set<AllowedResponseEntity> allowedResponses;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Question toDomain() {
        return Question.builder()
                .id(id)
                .responseCardinalityType(responseCardinalityType.toDomain())
                .sectionHeading(sectionHeading.toDomain())
                .allowedResponses(allowedResponses.stream()
                        .map(ent -> ent.toDomain())
                        .toList())
                .question(question)
                .build();
    }
}
