package gov.healthit.chpl.optionalStandard.entity;

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

import gov.healthit.chpl.certificationCriteria.CertificationCriterionEntity;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.optionalStandard.domain.OptionalStandardCriteriaMap;
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
@Table(name = "optional_standard_criteria_map")
public class OptionalStandardCriteriaMapEntity extends EntityAudit {
    private static final long serialVersionUID = -2526790160399898620L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "criterion_id")
    private Long certificationCriterionId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity criteria;

    @Column(name = "optional_standard_id")
    private Long optionalStandardId;

    @Basic(optional = false)
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "optional_standard_id", insertable = false, updatable = false)
    private OptionalStandardEntity optionalStandard;

    public OptionalStandardCriteriaMap toDomain() {
        return OptionalStandardCriteriaMap.builder()
                .id(this.getId())
                .optionalStandard(this.getOptionalStandard().toDomain())
                .criterion(this.getCriteria().toDomain())
                .build();
    }
}
