package gov.healthit.chpl.entity.surveillance;

import java.util.Date;
import java.util.HashSet;
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

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "pending_surveillance_requirement")
@Getter
@Setter
@ToString
public class PendingSurveillanceRequirementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "pending_surveillance_id")
    private Long pendingSurveillanceId;

    @Column(name = "type_value")
    private String requirementType;

    @Column(name = "requirement")
    private String surveilledRequirement;

    @Column(name = "certification_criterion_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterionEntity;

    @Column(name = "result_value")
    private String result;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "pendingSurveillanceRequirementId")
    @Basic(optional = false)
    @Column(name = "pending_surveillance_requirement_id", nullable = false)
    private Set<PendingSurveillanceNonconformityEntity> nonconformities = new HashSet<PendingSurveillanceNonconformityEntity>();
}
