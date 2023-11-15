package gov.healthit.chpl.subscription.entity;

import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriberRole;
import gov.healthit.chpl.subscription.domain.SubscriberStatus;
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
@Where(clause = "deleted <> 'true'")
public class SubscriberEntity extends EntityAudit {
    private static final long serialVersionUID = -4246212592490876084L;

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
