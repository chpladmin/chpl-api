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
import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "surveillance_requirement")
@Getter
@Setter
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
    @JoinColumn(name = "surveillance_requirement_detail_type", insertable = false, updatable = false)
    private RequirementDetailTypeEntity requirmentDetailType;

    @Column(name = "requirement_detail_other")
    private String requirementDetailOther;

    @Column(name = "result_id")
    private Long surveillanceResultTypeId;

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

    public SurveillanceRequirement toDomain() {
        SurveillanceRequirement req = SurveillanceRequirement.builder()
                .id(this.getId())
                .nonconformities(Optional.ofNullable(this.getNonconformities()).orElse(Collections.emptySet()).stream()
                        .map(e -> e.toDomain())
                        .toList())
                .requirementDetailType(this.requirmentDetailType.toDomain())
                .requirementDetailOther(this.requirementDetailOther)
                .result(this.getSurveillanceResultTypeEntity().toDomain())
                .build();

        switch (this.getRequirmentDetailType().getSurveillanceRequirementType().getId().intValue()) {
            case SurveillanceRequirementType.CERTIFIED_CAPABILITY_ID :

        }



        if (this.getCertificationCriterionEntity() != null) {
            CertificationCriterionEntity criterionEntity = this.getCertificationCriterionEntity();
            req.setRequirement(criterionEntity.getNumber());
            CertificationCriterion criterion = convertToDomain(criterionEntity);
            req.setCriterion(criterion);
        } else {
            req.setRequirement(this.getSurveilledRequirement());
        }

        if (this.getSurveillanceRequirementType() != null) {
            SurveillanceRequirementType result = new SurveillanceRequirementType();
            result.setId(this.getSurveillanceRequirementType().getId());
            result.setName(this.getSurveillanceRequirementType().getName());
            req.setType(result);
        } else {
            SurveillanceRequirementType result = new SurveillanceRequirementType();
            result.setId(this.getSurveillanceRequirementTypeId());
            req.setType(result);
        }



    }
}
