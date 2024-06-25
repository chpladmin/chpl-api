package gov.healthit.chpl.surveillance.report.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.surveillance.report.domain.SurveillanceOutcome;
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
@Table(name = "surveillance_outcome")
public class SurveillanceOutcomeEntity extends EntityAudit {
    private static final long serialVersionUID = -5020231056945542836L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name")
    private String name;

    public SurveillanceOutcome toDomain() {
        return SurveillanceOutcome.builder()
                .id(this.getId())
                .name(this.getName())
                .build();
    }
}
