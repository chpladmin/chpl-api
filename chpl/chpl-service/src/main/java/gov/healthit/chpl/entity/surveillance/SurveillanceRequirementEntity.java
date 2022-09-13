package gov.healthit.chpl.entity.surveillance;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
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

import gov.healthit.chpl.domain.CertificationCriterion;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirement;
import gov.healthit.chpl.domain.surveillance.SurveillanceRequirementType;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "surveillance_requirement")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveillanceRequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "surveillance_id")
    private Long surveillanceId;

    //@Column(name = "type_id")
    //private Long surveillanceRequirementTypeId;

    //@OneToOne(optional = true, fetch = FetchType.LAZY)
    //@JoinColumn(name = "type_id", insertable = false, updatable = false)
    //private SurveillanceRequirementTypeEntity surveillanceRequirementType;

    //@Column(name = "certification_criterion_id")
    //private Long certificationCriterionId;

    //@OneToOne(optional = true, fetch = FetchType.LAZY)
    //@JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    //private CertificationCriterionEntity certificationCriterionEntity;

    //@Column(name = "requirement")
    //private String surveilledRequirement;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "requirement_detail_type_id", insertable = true, updatable = true)
    private RequirementDetailTypeEntity requirementDetailType;

    @Column(name = "requirement_detail_other")
    private String requirementDetailOther;

    //@Column(name = "result_id")
    //private Long surveillanceResultTypeId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "result_id", insertable = false, updatable = false)
    private SurveillanceResultTypeEntity surveillanceResultTypeEntity;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "surveillanceRequirementId")
    @Basic(optional = false)
    @Column(name = "surveillance_requirement_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<SurveillanceNonconformityEntity> nonconformities = new HashSet<SurveillanceNonconformityEntity>();

    public SurveillanceRequirement toDomain(CertificationCriterionService certificationCriterionService) {
        SurveillanceRequirement req = SurveillanceRequirement.builder()
                .id(this.getId())
                .nonconformities(Optional.ofNullable(this.getNonconformities()).orElse(Collections.emptySet()).stream()
                        .map(e -> e.toDomain())
                        .toList())
                .requirementDetailType(this.requirementDetailType.toDomain())
                .requirementDetailOther(this.requirementDetailOther)
                .result(this.getSurveillanceResultTypeEntity().toDomain())
                .nonconformities(this.nonconformities.stream()
                        .map(e -> e.toDomain())
                        .toList())
                .type(SurveillanceRequirementType.builder()
                        .id(this.requirementDetailType.getSurveillanceRequirementType().getId())
                        .name(this.requirementDetailType.getSurveillanceRequirementType().getName())
                        .build())
                .build();

        int intValue = this.getRequirementDetailType().getSurveillanceRequirementType().getId().intValue();
        if (intValue == SurveillanceRequirementType.CERTIFIED_CAPABILITY_ID) {
            CertificationCriterion criterion = certificationCriterionService.get(req.getRequirementDetailType().getId());
            req.setCriterion(criterion);
            req.getType();
        } else if (intValue == SurveillanceRequirementType.TRANS_DISCLOSURE_ID || intValue == SurveillanceRequirementType.RWT_SUBMISSION_ID || intValue == SurveillanceRequirementType.ATTESTATION_SUBMISSION_ID) {
            req.setRequirement(req.getRequirementDetailType().getTitle());
        } else if (intValue == SurveillanceRequirementType.OTHER_ID) {
            req.setRequirement(req.getRequirementDetailOther());
        }

        return req;
    }
}
