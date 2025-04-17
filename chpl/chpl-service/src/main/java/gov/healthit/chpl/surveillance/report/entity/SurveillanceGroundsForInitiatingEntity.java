package gov.healthit.chpl.surveillance.report.entity;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceGroundsForInitiating;
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
@Table(name = "surveillance_grounds_for_initiating")
public class SurveillanceGroundsForInitiatingEntity extends EntityAudit {
    private static final long serialVersionUID = 5027397031593318299L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    public SurveillanceGroundsForInitiating toDomain() {
        return SurveillanceGroundsForInitiating.builder()
                .id(this.getId())
                .name(this.getName())
                .build();
    }
}
