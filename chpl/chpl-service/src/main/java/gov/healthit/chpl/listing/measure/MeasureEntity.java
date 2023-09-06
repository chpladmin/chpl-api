package gov.healthit.chpl.listing.measure;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Where;

import gov.healthit.chpl.certificationCriteria.CertificationCriterion;
import gov.healthit.chpl.domain.Measure;
import lombok.Data;

@Entity
@Data
@Table(name = "measure")
public class MeasureEntity {

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


    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

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
