package gov.healthit.chpl.listing.mipsMeasure;

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

import gov.healthit.chpl.domain.MipsMeasure;
import lombok.Data;

@Entity
@Data
@Table(name = "mips_measure")
public class MipsMeasureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic(optional = true)
    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "mips_domain_id", unique = true, nullable = true)
    private MipsMeasureDomainEntity domain;

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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "mipsMeasureId")
    @Basic(optional = false)
    @Column(name = "mips_measure_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<MipsMeasureCriterionMapEntity> allowedCriteria = new LinkedHashSet<MipsMeasureCriterionMapEntity>();


    @Column(name = "creation_date", nullable = false, updatable = false, insertable = false)
    private Date creationDate;

    @Column(nullable = false)
    private Boolean deleted;

    @Column(name = "last_modified_date", nullable = false, updatable = false, insertable = false)
    private Date lastModifiedDate;

    @Column(name = "last_modified_user", nullable = false)
    private Long lastModifiedUser;

    public MipsMeasure convert() {
        MipsMeasure measure = new MipsMeasure();
        measure.setId(getId());
        measure.setAbbreviation(getAbbreviation());
        measure.setName(getName());
        measure.setRequiredTest(getRequiredTest());
        measure.setRequiresCriteriaSelection(getCriteriaSelectionRequired());
        measure.setRemoved(getRemoved());
        measure.setDomain(getDomain().convert());
        if (getAllowedCriteria() != null) {
            getAllowedCriteria().stream()
                .forEach(allowedCriterion -> {
                    if (allowedCriterion != null) {
                        measure.getAllowedCriteria().add(allowedCriterion.convert());
                    }
                });
        }
        return measure;
    }
}
