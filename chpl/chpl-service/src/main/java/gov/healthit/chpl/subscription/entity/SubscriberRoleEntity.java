package gov.healthit.chpl.subscription.entity;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.subscription.domain.SubscriberRole;
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
@Table(name = "subscriber_role")
public class SubscriberRoleEntity extends EntityAudit {
    private static final long serialVersionUID = 3505581912787844922L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public SubscriberRole toDomain() {
        return SubscriberRole.builder()
                .id(getId())
                .name(getName())
                .sortOrder(getSortOrder())
                .build();
    }
}
