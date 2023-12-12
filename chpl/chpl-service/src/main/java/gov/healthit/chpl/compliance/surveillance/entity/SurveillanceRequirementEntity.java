package gov.healthit.chpl.compliance.surveillance.entity;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

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

import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.entity.EntityAudit;
import gov.healthit.chpl.service.CertificationCriterionService;
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
@Table(name = "surveillance_requirement")
public class SurveillanceRequirementEntity extends EntityAudit {
    private static final long serialVersionUID = 1726385155207715763L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "surveillance_id")
    private Long surveillanceId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_type_id")
    private RequirementTypeEntity requirementType;

    @Column(name = "requirement_type_other")
    private String requirementTypeOther;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id")
    private SurveillanceResultTypeEntity surveillanceResultTypeEntity;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "surveillanceRequirementId")
    @Column(name = "surveillance_requirement_id", nullable = false, insertable = false, updatable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<SurveillanceNonconformityEntity> nonconformities = new HashSet<SurveillanceNonconformityEntity>();

    public SurveillanceRequirement toDomain(CertificationCriterionService certificationCriterionService) {
        SurveillanceRequirement req = SurveillanceRequirement.builder()
                .id(this.getId())
                .nonconformities(Optional.ofNullable(this.getNonconformities()).orElse(Collections.emptySet()).stream()
                        .map(e -> e.toDomain(certificationCriterionService))
                        .toList())
                .requirementType(this.requirementType != null ? this.requirementType.toDomain() : null)
                .requirementTypeOther(this.requirementTypeOther)
                .result(this.getSurveillanceResultTypeEntity() != null ? this.getSurveillanceResultTypeEntity().toDomain() : null)
                .build();
        return req;
    }
}
