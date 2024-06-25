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
import gov.healthit.chpl.form.FormItem;
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
@Table(name = "form_item")
public class FormItemEntity extends EntityAudit {
    private static final long serialVersionUID = 3046925012842205688L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false, insertable = false, updatable = false)
    private FormEntity form;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false, insertable = false, updatable = false)
    private QuestionEntity question;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_form_item_id", nullable = true, insertable = false, updatable = false)
    private FormItemEntity parentFormItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_response_id", nullable = true, insertable = false, updatable = false)
    private AllowedResponseEntity parentResponse;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "required", nullable = false)
    private Boolean required;

    public FormItem toDomain() {
        return FormItem.builder()
                .id(id)
                .question(question.toDomain())
                .parentResponse(parentResponse != null ? parentResponse.toDomain() : null)
                .sortOrder(sortOrder)
                .required(required)
                .build();
    }
}
