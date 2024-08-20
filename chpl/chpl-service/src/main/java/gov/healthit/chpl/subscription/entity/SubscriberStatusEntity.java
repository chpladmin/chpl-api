package gov.healthit.chpl.subscription.entity;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.subscription.domain.SubscriberStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "subscriber_status")
public class SubscriberStatusEntity extends EntityAudit {
    private static final long serialVersionUID = -1148569431044665642L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public SubscriberStatus toDomain() {
        return SubscriberStatus.builder()
                .id(getId())
                .name(getName())
                .build();
    }
}
