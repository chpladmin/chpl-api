package gov.healthit.chpl.subscription.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.Subscription;
import gov.healthit.chpl.subscription.domain.SubscriptionConsolidationMethod;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscription")
@Where(clause = "deleted <> 'true'")
public class SubscriptionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "subscriber_id", nullable = false)
    private UUID subscriberId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_id", insertable = false, updatable = false)
    private SubscriberEntity subscriber;

    @Column(name = "subscription_subject_id")
    private Long subscriptionSubjectId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_subject_id", insertable = false, updatable = false)
    private SubscriptionSubjectEntity subscriptionSubject;

    @Column(name = "subscription_consolidation_method_id")
    private Long subscriptionConsolidationMethodId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_consolidation_method_id", insertable = false, updatable = false)
    private SubscriptionConsolidationMethodEntity subscriptionConsolidationMethod;

    @Column(name = "subscribed_object_id")
    private Long subscribedObjectId;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false, insertable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Subscription toDomain() {
        return Subscription.builder()
                .id(getId())
                .subscriber(getSubscriber() == null
                        ? Subscriber.builder().id(getSubscriberId()).build()
                                : getSubscriber().toDomain())
                .subject(getSubscriptionSubject() == null
                        ? SubscriptionSubject.builder().id(getSubscriptionSubjectId()).build()
                                : getSubscriptionSubject().toDomain())
                .consolidationMethod(getSubscriptionConsolidationMethod() == null
                        ? SubscriptionConsolidationMethod.builder().id(getSubscriptionConsolidationMethodId()).build()
                                : getSubscriptionConsolidationMethod().toDomain())
                .subscribedObjectId(getSubscribedObjectId())
                .build();
    }
}
