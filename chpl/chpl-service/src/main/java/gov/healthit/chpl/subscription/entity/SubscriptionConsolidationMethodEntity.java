package gov.healthit.chpl.subscription.entity;

import org.hibernate.annotations.Immutable;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.subscription.domain.SubscriptionConsolidationMethod;
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
@Table(name = "subscription_consolidation_method")
public class SubscriptionConsolidationMethodEntity extends EntityAudit {
    private static final long serialVersionUID = 3193374186456098737L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    public SubscriptionConsolidationMethod toDomain() {
        return SubscriptionConsolidationMethod.builder()
                .id(getId())
                .name(getName())
                .build();
    }
}
