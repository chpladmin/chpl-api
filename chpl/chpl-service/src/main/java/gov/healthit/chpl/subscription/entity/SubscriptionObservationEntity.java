package gov.healthit.chpl.subscription.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.DefaultUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.subscription.domain.Subscription;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
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
@Table(name = "subscription_observation")
@Where(clause = "deleted <> 'true'")
public class SubscriptionObservationEntity extends EntityAudit {
    private static final long serialVersionUID = 1444100672673955734L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new DefaultUserStrategy();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "subscription_observation_id_seq")
    @SequenceGenerator(name = "subscription_observation_id_seq", sequenceName = "subscription_observation_id_seq", allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "subscription_id", nullable = false)
    private Long subscriptionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", insertable = false, updatable = false)
    private SubscriptionEntity subscription;

    @Column(name = "activity_id", nullable = false)
    private Long activityId;

    public SubscriptionObservation toDomain() {
        return SubscriptionObservation.builder()
                .id(getId())
                .subscription(getSubscription() == null
                        ? Subscription.builder().id(getSubscriptionId()).build()
                                : getSubscription().toDomain())
                .activityId(getActivityId())
                .build();
    }
}
