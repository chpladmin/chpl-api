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

import org.hibernate.annotations.Where;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.NonconformityStatusEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "surveillance_nonconformity")
public class SurveillanceNonconformityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "surveillance_requirement_id")
    private Long surveillanceRequirementId;

    @Column(name = "certification_criterion_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterionEntity;

    @Column(name = "nonconformity_type")
    private String type;

    @Column(name = "nonconformity_status_id")
    private Long nonconformityStatusId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "nonconformity_status_id", insertable = false, updatable = false)
    private NonconformityStatusEntity nonconformityStatus;

    @Column(name = "date_of_determination")
    private Date dateOfDetermination;

    @Column(name = "corrective_action_plan_approval_date")
    private Date capApproval;

    @Column(name = "corrective_action_start_date")
    private Date capStart;

    @Column(name = "corrective_action_must_complete_date")
    private Date capMustCompleteDate;

    @Column(name = "corrective_action_end_date")
    private Date capEndDate;

    @Column(name = "non_conformity_close_date")
    private Date nonConformityCloseDate;

    @Column(name = "summary")
    private String summary;

    @Column(name = "findings")
    private String findings;

    @Column(name = "sites_passed")
    private Integer sitesPassed;

    @Column(name = "total_sites")
    private Integer totalSites;

    @Column(name = "developer_explanation")
    private String developerExplanation;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "deleted")
    private Boolean deleted;

    @Column(name = "last_modified_user")
    private Long lastModifiedUser;

    @Column(name = "creation_date", insertable = false, updatable = false)
    private Date creationDate;

    @Column(name = "last_modified_date", insertable = false, updatable = false)
    private Date lastModifiedDate;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "nonconformityId")
    @Basic(optional = false)
    @Column(name = "surveillance_nonconformity_id", nullable = false)
    @Where(clause = "deleted <> 'true'")
    private Set<SurveillanceNonconformityDocumentationEntity> documents = new HashSet<SurveillanceNonconformityDocumentationEntity>();

}
