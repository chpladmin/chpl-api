package gov.healthit.chpl.entity.surveillance;

import java.time.LocalDate;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import gov.healthit.chpl.entity.CertificationCriterionEntity;
import gov.healthit.chpl.entity.NonconformityTypeEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "pending_surveillance_nonconformity")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class PendingSurveillanceNonconformityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "pending_surveillance_requirement_id")
    private Long pendingSurveillanceRequirementId;

    @Column(name = "nonconformity_type")
    private String type;

    @Column(name = "certification_criterion_id")
    private Long certificationCriterionId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "certification_criterion_id", insertable = false, updatable = false)
    private CertificationCriterionEntity certificationCriterionEntity;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "nonconformity_type_id", insertable = false, updatable = false)
    private NonconformityTypeEntity ncType;

    @Column(name = "non_conformity_close_date")
    private LocalDate nonconformityCloseDate;

    @Column(name = "date_of_determination")
    private LocalDate dateOfDetermination;

    @Column(name = "corrective_action_plan_approval_date")
    private LocalDate capApproval;

    @Column(name = "corrective_action_start_date")
    private LocalDate capStart;

    @Column(name = "corrective_action_must_complete_date")
    private LocalDate capMustCompleteDate;

    @Column(name = "corrective_action_end_date")
    private LocalDate capEndDate;

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
}
