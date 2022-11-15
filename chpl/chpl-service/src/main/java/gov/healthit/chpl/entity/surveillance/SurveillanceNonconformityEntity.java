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

import gov.healthit.chpl.domain.surveillance.NonconformityClassification;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformity;
import gov.healthit.chpl.domain.surveillance.SurveillanceNonconformityStatus;
import gov.healthit.chpl.service.CertificationCriterionService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "surveillance_nonconformity")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SurveillanceNonconformityEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "surveillance_requirement_id")
    private Long surveillanceRequirementId;

    @OneToOne(optional = true, fetch = FetchType.LAZY)
    @JoinColumn(name = "nonconformity_type_id", insertable = true, updatable = true)
    private NonconformityTypeEntity type;

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

    @Column(name = "non_conformity_close_date")
    private LocalDate nonconformityCloseDate;

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

    public SurveillanceNonconformity toDomain(CertificationCriterionService certificationCriterionService) {
        SurveillanceNonconformity nc = SurveillanceNonconformity.builder()
                .capApprovalDay(this.getCapApproval())
                .capEndDay(this.getCapEndDate())
                .capMustCompleteDay(this.getCapMustCompleteDate())
                .capStartDay(this.getCapStart())
                .dateOfDeterminationDay(this.getDateOfDetermination())
                .nonconformityCloseDay(this.getNonconformityCloseDate())
                .developerExplanation(this.getDeveloperExplanation())
                .findings(this.getFindings())
                .id(this.getId())
                .type(this.getType().toDomain())
                .resolution(this.getResolution())
                .sitesPassed(this.getSitesPassed())
                .summary(this.getSummary())
                .totalSites(this.getTotalSites())
                .lastModifiedDate(this.getLastModifiedDate())
                .nonconformityStatus(this.getNonconformityCloseDate() == null ? SurveillanceNonconformityStatus.OPEN : SurveillanceNonconformityStatus.CLOSED)
                .nonconformityType(this.getType().getClassification().equals(NonconformityClassification.REQUIREMENT) ? this.getType().getNumber() : null)
                .build();

        if (nc.getType().getClassification().equals(NonconformityClassification.CRITERION)) {
            nc.setCriterion(certificationCriterionService.get(nc.getType().getId()));
        }

        return nc;
    }
}
