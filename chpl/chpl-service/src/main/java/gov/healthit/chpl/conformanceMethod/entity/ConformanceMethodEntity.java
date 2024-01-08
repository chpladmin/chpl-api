package gov.healthit.chpl.conformanceMethod.entity;

import java.time.LocalDate;
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
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethod;
import gov.healthit.chpl.entity.EntityAudit;
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
@Table(name = "conformance_method")
public class ConformanceMethodEntity extends EntityAudit {
    private static final long serialVersionUID = 4601144687597871604L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "name", nullable = false)
    private String name;

    @Basic(optional = true)
    @Column(name = "removal_date", nullable = true)
    private LocalDate removalDate;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "conformance_method_criteria_map",
        joinColumns = {@JoinColumn(name = "conformance_method_id", referencedColumnName = "id")},
        inverseJoinColumns = {@JoinColumn(name = "criteria_id", referencedColumnName = "certification_criterion_id")})
    @WhereJoinTable(clause = "deleted <> true")
    private List<CertificationCriterionEntity> criteria;

    public ConformanceMethod toDomain() {
        return ConformanceMethod.builder()
                .id(this.getId())
                .name(this.getName())
                .removalDate(this.getRemovalDate())
                .build();
    }

    public ConformanceMethod toDomainWithCriteria() {
        return ConformanceMethod.builder()
                .id(this.getId())
                .name(this.getName())
                .removalDate(this.getRemovalDate())
                .criteria(this.getCriteria().stream()
                        .map(crit -> crit.toDomain())
                        .collect(Collectors.toList()))
                .build();
    }
}
