package gov.healthit.chpl.listing.measure;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.Measure;
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
@Table(name = "measure")
public class MeasureEntity extends EntityAudit {
    private static final long serialVersionUID = -6519300005885154789L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "measure_domain_id", unique = true, nullable = true)
    private MeasureDomainEntity domain;

    @Column(name = "required_test_abbr")
    private String abbreviation;

    @Column(name = "required_test")
    private String requiredTest;

    @Column(name = "measure_name")
    private String name;

    @Column(name = "criteria_selection_required")
    private Boolean criteriaSelectionRequired;

    @Column(name = "removed")
    private Boolean removed;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "measureId")
    @Basic(optional = false)
    @Column(name = "measure_id", nullable = false)
    @Where(clause = " deleted = false ")
    private Set<MeasureCriterionMapEntity> allowedCriteria = new LinkedHashSet<MeasureCriterionMapEntity>();

    public Measure convert() {
        LinkedHashSet<CertificationCriterion> convertedAllowedCriteria = new LinkedHashSet<CertificationCriterion>();
        if (getAllowedCriteria() != null) {
            getAllowedCriteria().stream()
                .filter(allowedCriterion -> allowedCriterion != null)
                .forEach(allowedCriterion -> {
                    convertedAllowedCriteria.add(allowedCriterion.convert());
                });
        }

        return Measure.builder()
            .id(getId())
            .abbreviation(getAbbreviation())
            .name(getName())
            .requiredTest(getRequiredTest())
            .requiresCriteriaSelection(getCriteriaSelectionRequired())
            .removed(removed)
            .domain(getDomain().convert())
            .allowedCriteria(convertedAllowedCriteria)
            .build();
    }
}
