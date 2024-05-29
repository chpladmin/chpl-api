package gov.healthit.chpl.optionalStandard.entity;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.WhereJoinTable;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandard;
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
@Table(name = "optional_standard")
public class OptionalStandardEntity extends EntityAudit {
    private static final long serialVersionUID = 6507782927645169341L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "citation", nullable = false)
    private String citation;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "display_value", nullable = false)
    private String displayValue;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "optional_standard_criteria_map",
        joinColumns = {@JoinColumn(name = "optional_standard_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "criterion_id", referencedColumnName = "certification_criterion_id")})
    @WhereJoinTable(clause = "deleted <> true")
    private List<CertificationCriterionEntity> criteria;

    public OptionalStandard toDomain() {
        return OptionalStandard.builder()
                .id(this.getId())
                .citation(this.getCitation())
                .description(this.getDescription())
                .displayValue(this.getDisplayValue())
                .build();
    }

    public OptionalStandard toDomainWithCriteria() {
        return OptionalStandard.builder()
                .id(this.getId())
                .citation(this.getCitation())
                .description(this.getDescription())
                .displayValue(this.getDisplayValue())
                .criteria(this.getCriteria().stream().map(e -> e.toDomain()).collect(Collectors.toList()))
                .build();
    }
}
