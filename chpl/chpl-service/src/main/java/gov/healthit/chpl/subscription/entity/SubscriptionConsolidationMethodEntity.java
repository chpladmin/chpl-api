package gov.healthit.chpl.subscription.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.subscription.domain.SubscriptionConsolidationMethod;
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
@Where(clause = "deleted <> 'true'")
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
