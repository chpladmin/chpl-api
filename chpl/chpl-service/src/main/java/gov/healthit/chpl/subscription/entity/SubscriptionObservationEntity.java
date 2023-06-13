package gov.healthit.chpl.subscription.entity;

import java.util.Date;

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

import gov.healthit.chpl.subscription.domain.Subscription;
import gov.healthit.chpl.subscription.domain.SubscriptionObservation;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "subscription_observation")
@Where(clause = "deleted <> 'true'")
public class SubscriptionObservationEntity {

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

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false, insertable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

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
