package gov.healthit.chpl.subscription.entity;

import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.subscription.domain.Subscriber;
import gov.healthit.chpl.subscription.domain.SubscriberStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Immutable
@Table(name = "subscriber")
@Where(clause = "deleted <> 'true'")
public class SubscriberEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column(name = "subscriber_status_id")
    private Long subscriberStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "subscriber_status_id", insertable = false, updatable = false)
    private SubscriberStatusEntity subscriberStatus;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    @Column(name = "deleted", nullable = false, insertable = false)
    private Boolean deleted;

    @Column(name = "creation_date", nullable = false, insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", nullable = false, insertable = false, updatable = false)
    private Date lastModifiedDate;

    public Subscriber toDomain() {
        return Subscriber.builder()
                .id(getId())
                .email(getEmail())
                .status(getSubscriberStatus() == null
                        ? SubscriberStatus.builder().id(getSubscriberStatusId()).build()
                                : getSubscriberStatus().toDomain())
                .build();
    }
}
