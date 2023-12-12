package gov.healthit.chpl.functionalitytested;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    public FunctionalityTestedCriteriaMap toDomain() {
        return FunctionalityTestedCriteriaMap.builder()
                .id(id)
                .criterion(criterion.toDomain())
                .functionalityTested(functionalityTested.toDomainWithCriteria())
                .build();
    }
}
