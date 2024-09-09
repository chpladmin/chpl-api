package gov.healthit.chpl.subscription.entity;

import java.util.UUID;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.DefaultUserStrategy;
import gov.healthit.chpl.entity.lastmodifieduserstrategy.LastModifiedUserStrategy;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriberRole;
import gov.healthit.chpl.subscription.domain.SubscriberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
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
@Table(name = "subscriber")
public class SubscriberEntity extends EntityAudit {
    private static final long serialVersionUID = -4246212592490876084L;

    @Override
    public LastModifiedUserStrategy getLastModifiedUserStrategy() {
        return new DefaultUserStrategy();
    }


    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "subscriber_status_id", nullable = false)
    private Long subscriberStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_status_id", insertable = false, updatable = false)
    private SubscriberStatusEntity subscriberStatus;

    @Column(name = "subscriber_role_id")
    private Long subscriberRoleId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_role_id", insertable = false, updatable = false)
    private SubscriberRoleEntity subscriberRole;

    @Column(name = "email", nullable = false)
    private String email;

    public Subscriber toDomain() {
        return Subscriber.builder()
                .id(getId())
                .email(getEmail())
                .status(getSubscriberStatus() == null
                        ? SubscriberStatus.builder().id(getSubscriberStatusId()).build()
                                : getSubscriberStatus().toDomain())
                .role(getSubscriberRole() == null
                    ? SubscriberRole.builder().id(getSubscriberRoleId()).build()
                            : getSubscriberRole().toDomain())
                .build();
    }
}
