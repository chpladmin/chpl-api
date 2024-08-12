package gov.healthit.chpl.subscription.entity;

import java.util.Date;
import java.util.UUID;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.subscription.domain.SubscriptionObservationNotification;
import gov.healthit.chpl.util.DateUtil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "subscription_observation_notification")
public class SubscriptionObservationNotificationEntity {

    @Id
    private Long id;

    @Column(name = "subscriber_id", insertable = false, updatable = false)
    private UUID subscriberId;

    @Column(name = "subscriber_email", insertable = false, updatable = false)
    private String subscriberEmail;

    @Column(name = "subscriber_role", insertable = false, updatable = false)
    private String subscriberRole;

    @Column(name = "subscription_subject", insertable = false, updatable = false)
    private String subscriptionSubject;

    @Column(name = "subscription_object_type", insertable = false, updatable = false)
    private String subscriptionObjectType;

    @Column(name = "subscribed_object_id", insertable = false, updatable = false)
    private Long subscribedObjectId;

    @Column(name = "subscribed_object_name", insertable = false, updatable = false)
    private String subscribedObjectName;

    @Column(name = "certification_body_name", insertable = false, updatable = false)
    private String acbName;

    @Column(name = "developer_name", insertable = false, updatable = false)
    private String developerName;

    @Column(name = "product_name", insertable = false, updatable = false)
    private String productName;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "notified_at", nullable = false, insertable = false, updatable = false)
    private Date notificationDate;

    public SubscriptionObservationNotification toDomain() {
        return SubscriptionObservationNotification.builder()
                .creationDate(DateUtil.toLocalDateTime(getCreationDate().getTime()))
                .notificationDate(DateUtil.toLocalDateTime(getNotificationDate().getTime()))
                .subscribedObjectId(getSubscribedObjectId())
                .subscribedObjectName(getSubscribedObjectName())
                .subscriberEmail(getSubscriberEmail())
                .subscriberId(getSubscriberId())
                .subscriberRole(getSubscriberRole())
                .subscriptionObjectType(getSubscriptionObjectType())
                .subscriptionSubject(getSubscriptionSubject())
                .acbName(getAcbName())
                .developerName(getDeveloperName())
                .productName(getProductName())
                .build();
    }
}
