package gov.healthit.chpl.conformanceMethod.entity;

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
import gov.healthit.chpl.conformanceMethod.domain.ConformanceMethodCriteriaMap;
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
@Table(name = "conformance_method_criteria_map")
public class ConformanceMethodCriteriaMapEntity extends EntityAudit {
    private static final long serialVersionUID = -820698266815762652L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Column(name = "criteria_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "criteria_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterion;

    @Column(name = "conformance_method_id")
    private Long conformanceMethodId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "conformance_method_id", insertable = false, updatable = false)
    private ConformanceMethodEntity conformanceMethod;

    public ConformanceMethodCriteriaMap toDomain() {
        return ConformanceMethodCriteriaMap.builder()
                .id(this.getId())
                .conformanceMethod(this.getConformanceMethod().toDomain())
                .criterion(this.getCertificationCriterion().toDomain())
                .build();
    }
}
