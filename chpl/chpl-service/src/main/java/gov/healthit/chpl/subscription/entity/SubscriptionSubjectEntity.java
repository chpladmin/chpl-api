package gov.healthit.chpl.subscription.entity;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.subscription.domain.SubscriptionObjectType;
import gov.healthit.chpl.subscription.domain.SubscriptionSubject;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
@Immutable
@Table(name = "subscription_subject")
public class SubscriptionSubjectEntity extends EntityAudit {
    private static final long serialVersionUID = -4801635388305598783L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "subscription_object_type_id")
    private Long subscriptionObjectTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_object_type_id", insertable = false, updatable = false)
    private SubscriptionObjectTypeEntity subscriptionObjectType;

    @Column(name = "subject", nullable = false)
    private String subject;

    public SubscriptionSubject toDomain() {
        return SubscriptionSubject.builder()
                .id(getId())
                .type(getSubscriptionObjectType() == null
                    ? SubscriptionObjectType.builder().id(getSubscriptionObjectTypeId()).build()
                            : getSubscriptionObjectType().toDomain())
                .subject(getSubject())
                .build();
    }
}
