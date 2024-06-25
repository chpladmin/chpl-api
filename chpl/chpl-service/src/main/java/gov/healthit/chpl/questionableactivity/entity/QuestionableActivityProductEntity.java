package gov.healthit.chpl.questionableactivity.entity;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.domain.Product;
import gov.healthit.chpl.entity.ActivityEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.ProductEntity;
import gov.healthit.chpl.entity.auth.UserEntity;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityProduct;
import gov.healthit.chpl.questionableactivity.domain.QuestionableActivityTrigger;
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
@Table(name = "questionable_activity_product")
public class QuestionableActivityProductEntity extends EntityAudit implements QuestionableActivityBaseEntity {
    private static final long serialVersionUID = -1747920295763831472L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "questionable_activity_trigger_id")
    private Long triggerId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", insertable = true, updatable = false)
    private ActivityEntity activity;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "questionable_activity_trigger_id", insertable = false, updatable = false)
    private QuestionableActivityTriggerEntity trigger;

    @Column(name = "product_id")
    private Long productId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private ProductEntity product;

    @Column(name = "before_data")
    private String before;

    @Column(name = "after_data")
    private String after;

    @Column(name = "activity_date")
    private Date activityDate;

    @Column(name = "activity_user_id")
    private Long userId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_user_id", insertable = false, updatable = false)
    private UserEntity user;

    public QuestionableActivityProduct toDomain() {
        return QuestionableActivityProduct.builder()
                .id(this.getId())
                .activity(this.getActivity().toDomain())
                .trigger(this.getTrigger() == null
                    ? QuestionableActivityTrigger.builder().id(this.getTriggerId()).build()
                        : this.getTrigger().toDomain())
                .before(this.getBefore())
                .after(this.getAfter())
                .activityDate(this.getActivityDate())
                .productId(this.getProductId())
                .product(this.getProduct() == null
                    ? Product.builder().id(this.getProductId()).build()
                            : this.getProduct().toDomain())
                .build();
    }
}

