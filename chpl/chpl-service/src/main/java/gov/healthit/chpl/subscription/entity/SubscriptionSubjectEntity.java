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
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.subscription.domain.SubscriptionSubject;
import gov.healthit.chpl.subscription.domain.SubscriptionType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "subscription_subject")
@Where(clause = "deleted <> 'true'")
public class SubscriptionSubjectEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "subscription_type_id")
    private Long subscriptionTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_type_id", insertable = false, updatable = false)
    private SubscriptionTypeEntity subscriptionType;

    @Column(name = "subject", nullable = false)
    private String subject;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    public SubscriptionSubject toDomain() {
        return SubscriptionSubject.builder()
                .id(getId())
                .type(getSubscriptionType() == null
                    ? SubscriptionType.builder().id(getSubscriptionTypeId()).build()
                            : getSubscriptionType().toDomain())
                .subject(getSubject())
                .build();
    }
}
