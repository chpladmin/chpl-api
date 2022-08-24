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

import gov.healthit.chpl.form.FormItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "form_item")
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FormItemEntity {
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

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

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
