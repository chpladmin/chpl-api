package gov.healthit.chpl.subscription.entity;

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

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.DefaultUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.Subscription;
import gov.healthit.chpl.subscription.domain.SubscriptionConsolidationMethod;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;
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
@Table(name = "subscription")
@Where(clause = "deleted <> 'true'")
public class SubscriptionEntity extends EntityAudit {
    private static final long serialVersionUID = -1705305416503500650L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new DefaultUserStrategy();
    }


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
                .creationDate(getCreationDate())
                .build();
    }
}
