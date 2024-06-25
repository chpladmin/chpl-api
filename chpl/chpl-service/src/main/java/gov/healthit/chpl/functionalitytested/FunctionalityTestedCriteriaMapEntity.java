package gov.healthit.chpl.functionalitytested;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import gov.healthit.chpl.certificationCriteria.CertificationCriterionComparator;
import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
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
@Table(name = "functionality_tested_criteria_map")
public class FunctionalityTestedCriteriaMapEntity extends EntityAudit {
    private static final long serialVersionUID = 6446486138564063907L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "criteria_id")
    private Long certificationCriterionId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", insertable = false, updatable = false)
    private CertificationCriterionEntity criterion;

    @Column(name = "functionality_tested_id")
    private Long functionalityTestedId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "functionality_tested_id", insertable = false, updatable = false)
    private FunctionalityTestedEntity functionalityTested;

    public FunctionalityTestedCriteriaMap toDomain(CertificationCriterionComparator criterionComparator) {
        return FunctionalityTestedCriteriaMap.builder()
                .id(id)
                .criterion(criterion.toDomain())
                .functionalityTested(functionalityTested.toDomainWithCriteria(criterionComparator))
                .build();
    }
}
